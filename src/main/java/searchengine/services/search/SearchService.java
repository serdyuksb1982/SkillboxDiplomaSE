package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.dto.SearchDto;
import searchengine.lemma.LemmaEngine;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final LemmaEngine morphology;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;

    private List<SearchDto> getSearchDtoList(List<LemmaModel> lemmaList,
                                             List<String> textLemmaList,
                                             int start, int limit) {
        List<SearchDto> result = new ArrayList<>();
        pageRepository.flush();
        if (lemmaList.size() >= textLemmaList.size()) {
            List<PageModel> pagesList = pageRepository.findByLemmaList(lemmaList);
            indexRepository.flush();
            List<IndexModel> indexesList = indexRepository.findByPageAndLemmas(lemmaList, pagesList);
            ConcurrentHashMap<PageModel, Float> relevanceMap = getPageAbsRelevance(pagesList, indexesList);
            List<SearchDto> searchDtos = getSearchData(relevanceMap, textLemmaList);
            if (start > searchDtos.size()) {
                return new ArrayList<>();
            }
            if (searchDtos.size() > limit) {
                for (int i = start; i < limit; i++) {
                    result.add(searchDtos.get(i));
                }
                return result;
            } else return searchDtos;

        } else return result;
    }

    private List<SearchDto> getSearchData(ConcurrentHashMap<PageModel, Float> pageList, List<String> textLemmaList) {
        List<SearchDto> result = new ArrayList<>();
        for (PageModel page : pageList.keySet()) {
            String uri = page.getPath();
            String content = page.getContent();
            SiteModel pageSite = page.getSiteId();
            String site = pageSite.getUrl();
            String siteName = pageSite.getName();
            StringBuilder stringBuilder = new StringBuilder();
            String title = clearHtmlCode(content, "title");
            String body = clearHtmlCode(content, "body");
            stringBuilder.append(title).append(body);
            float absRel = pageList.get(page);
            String snipped = getSnippet(stringBuilder.toString(), textLemmaList);
            result.add(new SearchDto(site, siteName, uri, title, snipped, absRel));
        }
        return result;
    }

    private String getSnippet(String content, List<String> lemmaList) {
        List<Integer> lemmaIndex = new ArrayList<>();
        StringBuilder result = new StringBuilder();
        for (String lemma : lemmaList) {
            lemmaIndex.addAll(morphology.findLemmaIndexInText(content, lemma));
        }
        Collections.sort(lemmaIndex);
        List<String> wordList = getWordsFromContent(content, lemmaIndex);
        for (int i = 0; i < wordList.size(); i++) {
            result.append(wordList.get(i)).append("...");
            if (i > 3) {
                break;
            }
        }
        return result.toString();
    }

    private List<String> getWordsFromContent(String content, List<Integer> lemmaIndex) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < lemmaIndex.size(); i++) {
            int start = lemmaIndex.get(i);
            int end = content.indexOf(" ", start);
            int next = i + 1;
            while ((next < lemmaIndex.size()) && (lemmaIndex.get(next) - end > 0) && (lemmaIndex.get(next) - end < 5)) {
                end = content.indexOf(" ", lemmaIndex.get(next));
                next = next + 1;
            }
            i = next - 1;

            String word = content.substring(start, end);
            int startIndex;
            int nextIndex;
            if (content.lastIndexOf(" ", start) != -1) {
                startIndex = content.lastIndexOf(" ", start);
            } else startIndex = start;
            if (content.indexOf(" ", end  + 30) != -1) {
                nextIndex = content.indexOf(" ", end + 30);
            } else nextIndex = content.indexOf(" ", end);
            String text = content.substring(startIndex, nextIndex);

            text = text.replaceAll(word, "<b>".concat(word).concat("</b>"));


            result.add(text);
        }
        result.sort(Comparator.comparing(String::length).reversed());
        return result;
    }

    private ConcurrentHashMap<PageModel, Float> getPageAbsRelevance(List<PageModel> pageList,
                                                                    List<IndexModel> indexList) {
        Map<PageModel, Float> relevanceMap = new HashMap<>();
        for (PageModel page : pageList) {
            float relevance = 0;
            for (IndexModel index : indexList) {
                if (index.getPage() == page) {
                    relevance += index.getRank();
                }
            }
            relevanceMap.put(page, relevance);
        }
        Map<PageModel, Float> allRelevanceMap = new HashMap<>();
        for (PageModel page : relevanceMap.keySet()) {
            float relevance = relevanceMap.get(page) / Collections.max(relevanceMap.values());
            allRelevanceMap.put(page, relevance);
        }
        List<Map.Entry<PageModel, Float>> sortList = new ArrayList<>();
        for (Map.Entry<PageModel, Float> map : allRelevanceMap
                .entrySet()) {
            sortList.add(map);
        }
        sortList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        ConcurrentHashMap<PageModel, Float> map = new ConcurrentHashMap<>();
        for (Map.Entry<PageModel, Float> pageModelFloatEntry : sortList) {
            map.putIfAbsent(pageModelFloatEntry.getKey(), pageModelFloatEntry.getValue());
        }
        return map;
    }

    private List<LemmaModel> getLemmaListFromSite(List<String> lemmas, SiteModel site) {
        lemmaRepository.flush();
        List<LemmaModel> lemmaModels = lemmaRepository.findLemmaListBySite(lemmas, site);
        List<LemmaModel> result = new ArrayList<>(lemmaModels);
        result.sort(Comparator.comparingInt(LemmaModel::getFrequency));
        return result;
    }

    private List<String> getLemmaFromSearchText(String text) {
        String[] words = text.toLowerCase(Locale.ROOT).split(" ");
        List<String> lemmaList = new ArrayList<>();
        for (String lemma : words) {
            try {
                List<String> list = morphology.getLemma(lemma);
                lemmaList.addAll(list);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return lemmaList;
    }

    public List<SearchDto> siteSearch(String text,
                                      String url,
                                      int start,
                                      int limit) {
        SiteModel site = siteRepository.findByUrl(url);
        List<String> textLemmaList = getLemmaFromSearchText(text);
        List<LemmaModel> foundLemmaList = getLemmaListFromSite(textLemmaList, site);
        return getSearchDtoList(foundLemmaList, textLemmaList, start, limit);
    }

    public List<SearchDto> fullSiteSearch(String text,
                                          int start,
                                          int limit) {
        List<SiteModel> siteList = siteRepository.findAll();
        List<SearchDto> result = new ArrayList<>();
        List<LemmaModel> foundLemmaList = new ArrayList<>();
        List<String> textLemmaList = getLemmaFromSearchText(text);
        for (SiteModel site : siteList) {
            foundLemmaList.addAll(getLemmaListFromSite(textLemmaList, site));
        }
        List<SearchDto> searchData = null;
        for (LemmaModel l : foundLemmaList) {
            if (l.getLemma().equals(text)) {
                searchData = new ArrayList<>(getSearchDtoList(foundLemmaList, textLemmaList, start, limit));
                searchData.sort(new Comparator<SearchDto>() {
                    @Override
                    public int compare(SearchDto o1, SearchDto o2) {
                        return Float.compare(o2.getRelevance(), o1.getRelevance());
                    }
                });
                if (searchData.size() > limit) {
                    for (int i = start; i < limit; i++) {
                        result.add(searchData.get(i));
                    }
                    return result;
                }
            } else {
                try {
                    throw new Exception();
                } catch (Exception e) {
                    throw new RuntimeException();
                }
            }
        }
        return searchData;
    }

    public  String clearHtmlCode(String text, String element) {
        StringBuilder stringBuilder = new StringBuilder();
        Document doc = Jsoup.parse(text);
        Elements elements = doc.select(element);
        for (Element el : elements) {
            stringBuilder.append(el.html());
        }
        return Jsoup.parse(stringBuilder.toString()).text();
    }
}

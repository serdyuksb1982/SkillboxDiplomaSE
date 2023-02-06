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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final LemmaEngine lemmaEngine;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;

    private List<SearchDto> getSearchDtoList(ConcurrentHashMap<PageModel, Float> pageList,
                                             List<String> textLemmaList) {
        List<SearchDto> searchDtoList = new ArrayList<>();
        Iterator<PageModel> iterator = pageList.keySet().iterator();
        while (iterator.hasNext()) {
            PageModel page = iterator.next();
            String uri = page.getPath();
            String content = page.getContent();
            SiteModel pageSite = page.getSiteId();
            String site = pageSite.getUrl();
            String siteName = pageSite.getName();
            StringBuilder stringBuilder = new StringBuilder();
            String title = clearHtmlCode(content, "title");
            String body = clearHtmlCode(content, "body");
            stringBuilder.append(title).append(body);
            float value = pageList.get(page);
            List<Integer> lemmaIndex = new ArrayList<>();
            StringBuilder snippetBuilder = new StringBuilder();
            {
                int i = 0;
                while (i < textLemmaList.size()) {
                    String lemma = textLemmaList.get(i);
                    try {
                        lemmaIndex.addAll(lemmaEngine.findLemmaIndexInText(stringBuilder.toString(), lemma));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    i++;
                }
            }
            Collections.sort(lemmaIndex);
            List<String> wordList = getWordsFromSiteContent(stringBuilder.toString(), lemmaIndex);
            int i = 0;
            while (i < wordList.size()) {
                snippetBuilder.append(wordList.get(i)).append(".");
                if (i > 3) {
                    break;
                }
                i++;
            }

            searchDtoList.add(new SearchDto(site, siteName, uri, title, snippetBuilder.toString(), value));
        }
        return searchDtoList;
    }

    private List<String> getWordsFromSiteContent(String content, List<Integer> lemmaIndex) {
        List<String> result = new ArrayList<>();
        int i = 0;
        while ( i < lemmaIndex.size()) {
            int start = lemmaIndex.get(i);
            int end = content.indexOf(" ", start);
            int next = i + 1;
            while (!(!(next < lemmaIndex.size()) || !(lemmaIndex.get(next) - end > 0) || !(lemmaIndex.get(next) - end < 5))) {
                end = content.indexOf(" ", lemmaIndex.get(next));
                next += 1;
            }
            i = next - 1;
            String word = content.substring(start, end);
            int startIndex;
            int nextIndex;
            if (content.lastIndexOf(" ", start) != -1) {
                startIndex = content.lastIndexOf(" ", start);
            } else startIndex = start;
            if (content.indexOf(" ", end  + lemmaIndex.size() / 10) != -1) {
                nextIndex = content.indexOf(" ", end + lemmaIndex.size() / 10);
            } else nextIndex = content.indexOf(" ", end);
            String text = content.substring(startIndex, nextIndex).replaceAll(word, "<b>".concat(word).concat("</b>"));
            result.add(text);
            i++;
        }
        result.sort(Comparator.comparing(String::length).reversed());
        return result;
    }

    private Map<PageModel, Float> getRelevanceFromPage(List<PageModel> pageList,
                                                      List<IndexModel> indexList) {
        Map<PageModel, Float> relevanceMap = new HashMap<>();
        {
            int i = 0;
            while (i < pageList.size()) {
                PageModel page = pageList.get(i);
                float relevance = 0;
                int j = 0;
                while (j < indexList.size()) {
                    IndexModel index = indexList.get(j);
                    if (index.getPage() == page) {
                        relevance += index.getRank();
                    }
                    j++;
                }
                relevanceMap.put(page, relevance);
                i++;
            }
        }
        Map<PageModel, Float> allRelevanceMap = new HashMap<>();
        {
            Iterator<PageModel> iterator = relevanceMap.keySet().iterator();
            while (iterator.hasNext()) {
                PageModel page = iterator.next();
                float relevance = relevanceMap.get(page) / Collections.max(relevanceMap.values());
                allRelevanceMap.put(page, relevance);
            }
        }
        List<Map.Entry<PageModel, Float>> sortList = new ArrayList<>();
        Iterator<Map.Entry<PageModel, Float>> iterator = allRelevanceMap
                .entrySet().iterator();
        while (iterator.hasNext()) {
        Map.Entry<PageModel, Float> map = iterator.next();
        sortList.add(map);
}
        sortList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        Map<PageModel, Float> map = new ConcurrentHashMap<>();
        int i = 0;
        while (i < sortList.size()) {
            Map.Entry<PageModel, Float> pageModelFloatEntry = sortList.get(i);
            map.putIfAbsent(pageModelFloatEntry.getKey(), pageModelFloatEntry.getValue());
            i++;
        }
        return map;
    }

    public List<LemmaModel> getLemmaModelFromSite(List<String> lemmas, SiteModel site) {
        lemmaRepository.flush();
        List<LemmaModel> lemmaModels = lemmaRepository.findLemmaListBySite(lemmas, site);
        List<LemmaModel> result = new ArrayList<>(lemmaModels);
        result.sort(Comparator.comparingInt(LemmaModel::getFrequency));
        return result;
    }

    public List<String> getLemmaFromSearchText(String text) {
        String[] words = text.toLowerCase(Locale.ROOT).split(" ");
        List<String> lemmaList = new ArrayList<>();
        int i = 0;
        while (i < words.length) {
            String lemma = words[i];
            try {
                List<String> list = lemmaEngine.getLemma(lemma);
                lemmaList.addAll(list);
            } catch (Exception e) {
                e.getMessage();
            }
            i++;
        }
        return lemmaList;
    }

    public List<SearchDto> createSearchDtoList(List<LemmaModel> lemmaList,
                                               List<String> textLemmaList,
                                               int start, int limit) {
        List<SearchDto> result = new ArrayList<>();
        pageRepository.flush();
        if (lemmaList.size() >= textLemmaList.size()) {
            List<PageModel> pagesList = pageRepository.findByLemmaList(lemmaList);
            indexRepository.flush();
            List<IndexModel> indexesList = indexRepository.findByPageAndLemmas(lemmaList, pagesList);
            Map<PageModel, Float> relevanceMap = getRelevanceFromPage(pagesList, indexesList);
            List<SearchDto> searchDtos = getSearchDtoList((ConcurrentHashMap<PageModel, Float>) relevanceMap, textLemmaList);
            if (start > searchDtos.size()) {
                return new ArrayList<>();
            }
            if (searchDtos.size() > limit) {
                int i = start;
                while (i < limit) {
                    result.add(searchDtos.get(i));
                    i++;
                }
                return result;
            } else return searchDtos;

        } else return result;
    }

    public  String clearHtmlCode(String text, String element) {
        String stringBuilder;
        Document doc = Jsoup.parse(text);
        Elements elements = doc.select(element);
        stringBuilder = elements.stream().map(Element::html).collect(Collectors.joining());
        return Jsoup.parse(stringBuilder).text();
    }
}

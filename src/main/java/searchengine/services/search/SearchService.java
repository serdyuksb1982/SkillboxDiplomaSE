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


    private String getWordsFromIndex(int start, int end, String content) {
        String word = content.substring(start, end);
        int prevPoint;
        int lastPoint;
        if (content.lastIndexOf(" ", start) != -1) {
            prevPoint = content.lastIndexOf(" ", start);
        } else prevPoint = start;
        if (content.indexOf(" ", end  + 30) != -1) {
            lastPoint = content.indexOf(" ", end + 30);
        } else lastPoint = content.indexOf(" ", end);
        String text = content.substring(prevPoint, lastPoint);

        text = text.replaceAll(word, "<b>" + word + "</b>");
        return text;

    }

    private List<SearchDto> getSearchDtoList(List<LemmaModel> lemmaList,
                                             List<String> textLemmaList,
                                             int offset, int limit) {
        List<SearchDto> result = new ArrayList<>();
        pageRepository.flush();
        if (lemmaList.size() >= textLemmaList.size()) {
            List<PageModel> foundPageList = pageRepository.findByLemmaList(lemmaList);
            indexRepository.flush();
            List<IndexModel> foundIndexList = indexRepository.findByPageAndLemmas(lemmaList, foundPageList);
            ConcurrentHashMap<PageModel, Float> sortedPageByAbsRelevance = getPageAbsRelevance(foundPageList, foundIndexList);
            List<SearchDto> searchDtos = getSearchData(sortedPageByAbsRelevance, textLemmaList);
            if (offset > searchDtos.size()) {
                return new ArrayList<>();
            }
            if (searchDtos.size() > limit) {
                for (int i = offset; i < limit; i++) {
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
            String title = clearCodeFromTag(content, "title");
            String body = clearCodeFromTag(content, "body");
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
            int nextPoint = i + 1;
            while (nextPoint < lemmaIndex.size() && lemmaIndex.get(nextPoint) - end > 0 && lemmaIndex.get(nextPoint) - end < 5) {
                end = content.indexOf(" ", lemmaIndex.get(nextPoint));
                nextPoint += 1;
            }
            i = nextPoint - 1;
            String text = getWordsFromIndex(start, end, content);
            result.add(text);
        }
        result.sort(Comparator.comparing(String::length).reversed());
        return result;
    }

    private ConcurrentHashMap<PageModel, Float> getPageAbsRelevance(List<PageModel> pageList, List<IndexModel> indexList) {
        Map<PageModel, Float> pageWithRelevance = new HashMap<>();
        for (PageModel page : pageList) {
            float relevant = 0;
            for (IndexModel index : indexList) {
                if (index.getPage() == page) {
                    relevant += index.getRank();
                }
            }
            pageWithRelevance.put(page, relevant);
        }
        Map<PageModel, Float> pageWithAbsoluteRelevance = new HashMap<>();
        for (PageModel page : pageWithRelevance.keySet()) {
            float absoluteRelevance = pageWithRelevance.get(page) / Collections.max(pageWithRelevance.values());
            pageWithAbsoluteRelevance.put(page, absoluteRelevance);
        }
        List<Map.Entry<PageModel, Float>> toSort = new ArrayList<>();
        for (Map.Entry<PageModel, Float> pageModelFloatEntry : pageWithAbsoluteRelevance
                .entrySet()) {
            toSort.add(pageModelFloatEntry);
        }
        toSort.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        ConcurrentHashMap<PageModel, Float> map = new ConcurrentHashMap<>();
        for (Map.Entry<PageModel, Float> pageModelFloatEntry : toSort) {
            map.putIfAbsent(pageModelFloatEntry.getKey(), pageModelFloatEntry.getValue());
        }
        return map;
    }

    public  String clearCodeFromTag(String content, String s) {
        StringBuilder stringBuilder = new StringBuilder();
        Document doc = Jsoup.parse(content);
        Elements elements = doc.select(s);
        for (Element el : elements) {
            stringBuilder.append(el.html());
        }
        return Jsoup.parse(stringBuilder.toString()).text();
    }

    private List<LemmaModel> getLemmaListFromSite(List<String> lemmas, SiteModel site) {
        lemmaRepository.flush();
        List<LemmaModel> lemmaModels = lemmaRepository.findLemmaListBySite(lemmas, site);
        List<LemmaModel> result = new ArrayList<>(lemmaModels);
        result.sort(Comparator.comparingInt(LemmaModel::getFrequency));
        return result;
    }

    private List<String> getLemmaFromSearchText(String searchText) {
        String[] words = searchText.toLowerCase(Locale.ROOT).split(" ");
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

    public List<SearchDto> siteSearch(String searchText, String url, int offset, int limit) {
        log.info("Search in ".concat(searchText).concat("\\").concat(" site ").concat(url));
        SiteModel site = siteRepository.findByUrl(url);
        List<String> textLemmaList = getLemmaFromSearchText(searchText);
        List<LemmaModel> foundLemmaList = getLemmaListFromSite(textLemmaList, site);
        log.info("Search ended.");
        return getSearchDtoList(foundLemmaList, textLemmaList, offset, limit);
    }

    public List<SearchDto> allSiteSearch(String searchText, int offset, int limit) {
        log.info("Search in ".concat(searchText).concat("\\").concat(" site "));
        List<SiteModel> siteList = siteRepository.findAll();
        List<SearchDto> result = new ArrayList<>();
        List<LemmaModel> foundLemmaList = new ArrayList<>();
        List<String> textLemmaList = getLemmaFromSearchText(searchText);
        for (SiteModel site : siteList) {
            foundLemmaList.addAll(getLemmaListFromSite(textLemmaList, site));
        }
        List<SearchDto> searchData = null;
        for (LemmaModel l : foundLemmaList) {
            if (l.getLemma().equals(searchText)) {
                searchData = new ArrayList<>(getSearchDtoList(foundLemmaList, textLemmaList, offset, limit));
                searchData.sort((o1, o2) -> Float.compare(o2.getActuality(), o1.getActuality()));
                if (searchData.size() > limit) {
                    for (int i = offset; i < limit; i++) {
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
        log.info("Поисковый запрос обработан. Ответ получен.");
        return searchData;
    }
}

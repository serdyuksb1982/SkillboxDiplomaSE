package searchengine.services.search;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.dto.SearchDto;
import searchengine.exception.CurrentIOException;
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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public record SearchService(LemmaEngine lemmaEngine, LemmaRepository lemmaRepository, PageRepository pageRepository, IndexRepository indexRepository) {

    private List<SearchDto> getSearchDtoList(ConcurrentHashMap<PageModel, Float> pageList,
                                             List<String> textLemmaList) {
        List<SearchDto> searchDtoList = new ArrayList<>();
        StringBuilder titleStringBuilder = new StringBuilder();
        for (PageModel page : pageList.keySet()) {
            String uri = page.getPath();
            String content = page.getContent();
            SiteModel pageSite = page.getSiteId();
            String site = pageSite.getUrl();
            String siteName = pageSite.getName();
            String title = clearCodeFromTag(content, "title");
            String body = clearCodeFromTag(content, "body");
            titleStringBuilder.append(title).append(body);
            float pageValue = pageList.get(page);
            List<Integer> lemmaIndex = new ArrayList<>();
            int i = 0;
            while (i < textLemmaList.size()) {
                String lemma = textLemmaList.get(i);
                try {
                    lemmaIndex.addAll(lemmaEngine.findLemmaIndexInText(titleStringBuilder.toString(), lemma));
                } catch (IOException e) {
                    new CurrentIOException(e.getMessage());
                }
                i++;
            }
            Collections.sort(lemmaIndex);
            StringBuilder snippetBuilder = new StringBuilder();
            List<String> wordList = getWordsFromSiteContent(titleStringBuilder.toString(), lemmaIndex);
            int y = 0;
            while (y < wordList.size()) {
                snippetBuilder.append(wordList.get(y)).append(".");
                if (y > 3) {
                    break;
                }
                y++;
            }
            searchDtoList.add(new SearchDto(site, siteName, uri, title, snippetBuilder.toString(), pageValue));
        }
        return searchDtoList;
    }

    private List<String> getWordsFromSiteContent(String content, List<Integer> lemmaIndex) {
        List<String> result = new ArrayList<>();
        int i = 0;
        while (i < lemmaIndex.size()) {
            int start = lemmaIndex.get(i);
            int end = content.indexOf(" ", start);
            int next = i + 1;
            while (next < lemmaIndex.size() && 0 < lemmaIndex.get(next) - end && lemmaIndex.get(next) - end < 5) {
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
            if (content.indexOf(" ", (end + lemmaIndex.size() / (lemmaIndex.size() / 10))) != -1) {
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

        int i = 0;
        int j = 0;
        while (i < pageList.size()) {
            PageModel page = pageList.get(i);
            float relevance = 0;
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

        Map<PageModel, Float> allRelevanceMap = new HashMap<>();

        relevanceMap.keySet().forEach(page -> {
            float relevance = relevanceMap.get(page) / Collections.max(relevanceMap.values());
            allRelevanceMap.put(page, relevance);
        });

        List<Map.Entry<PageModel, Float>> sortList = new ArrayList<>(allRelevanceMap.entrySet());
        sortList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        Map<PageModel, Float> map = new ConcurrentHashMap<>();
        Entry<PageModel, Float> pageModelFloatEntry;
        int y = 0;
        while (y < sortList.size()) {
            pageModelFloatEntry = sortList.get(y);
            map.putIfAbsent(pageModelFloatEntry.getKey(), pageModelFloatEntry.getValue());
            y++;
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
        List<String> list;
        while (i < words.length) {
            String lemma = words[i];
            try {
                list = lemmaEngine.getLemma(lemma);
                lemmaList.addAll(list);
            } catch (IOException e) {
                new CurrentIOException(e.getMessage());
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

    public  String clearCodeFromTag(String text, String element) {
        Document doc = Jsoup.parse(text);
        Elements elements = doc.select(element);
        String html = elements.stream().map(Element::html).collect(Collectors.joining());
        return Jsoup.parse(html).text();
    }
}

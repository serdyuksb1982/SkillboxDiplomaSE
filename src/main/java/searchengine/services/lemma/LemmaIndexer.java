package searchengine.services.lemma;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import searchengine.dto.LemmaDto;
import searchengine.lemma.LemmaEngine;
import searchengine.model.PageModel;
import searchengine.repository.PageRepository;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Getter
public class LemmaIndexer {
    private final PageRepository pageRepository;
    private final LemmaEngine lemmaEngine;
    private List<LemmaDto> lemmaDtoList;

    public void startLemmaIndexer() {
        lemmaDtoList = new CopyOnWriteArrayList<>();
        Iterable<PageModel> pageList = pageRepository.findAll();

        Map<String, Integer> lemmaList = new TreeMap<>();
        Map<String, Integer> titleSiteList;
        Map<String, Integer> bodySiteList;
        Set<String> allWordsInIndexingSite;

        for (var page : pageList) {
            String content = page.getContent();
            String title = clearCodeFromTag(content, "title");
            String body = clearCodeFromTag(content, "body");
            titleSiteList = lemmaEngine.getLemmaMap(title);
            bodySiteList = lemmaEngine.getLemmaMap(body);
            allWordsInIndexingSite = new HashSet<>();
            allWordsInIndexingSite.addAll(titleSiteList.keySet());
            allWordsInIndexingSite.addAll(bodySiteList.keySet());
            allWordsInIndexingSite.forEach(word -> {
                int frequency = lemmaList.getOrDefault(word, 0) + 1;
                lemmaList.put(word, frequency);
            });
        }
        lemmaList.keySet().forEach(lemma -> {
            Integer frequency = lemmaList.get(lemma);
            lemmaDtoList.add(new LemmaDto(lemma, frequency));
        });
    }

    public String clearCodeFromTag(String content, String tag) {
        String html;
        Document doc = Jsoup.parse(content);
        Elements elements = doc.select(tag);
        html = elements.stream().map(Element::html).collect(Collectors.joining());
        return Jsoup.parse(html).text();
    }
}

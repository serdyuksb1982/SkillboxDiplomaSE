package searchengine.services.lemma;

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
import searchengine.model.SiteModel;
import searchengine.repository.PageRepository;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class LemmaIndexer {
    private final PageRepository pageRepository;
    private final LemmaEngine morphology;
    private List<LemmaDto> lemmaDtoList;


    public void run(SiteModel site) {
        lemmaDtoList = new CopyOnWriteArrayList<>();
        Iterable<PageModel> pageList = pageRepository.findAll();
        TreeMap<String, Integer> lemmaList = new TreeMap<>();
        for (PageModel page : pageList) {
            String content = page.getContent();
            String title = clearHtml(content, "title");
            String body = clearHtml(content, "body");
            HashMap<String, Integer> titleList = morphology.getLemmaList(title);
            HashMap<String, Integer> bodyList = morphology.getLemmaList(body);
            Set<String> allWords = new HashSet<>();
            allWords.addAll(titleList.keySet());
            allWords.addAll(bodyList.keySet());
            for (String word : allWords) {
                int frequency = lemmaList.getOrDefault(word, 0) + 1;
                lemmaList.put(word, frequency);
            }
        }
        for (String lemma : lemmaList.keySet()) {
            Integer frequency = lemmaList.get(lemma);
            lemmaDtoList.add(new LemmaDto(lemma, frequency));
        }
    }


    public List<LemmaDto> getLemmaDtoList() {
        return lemmaDtoList;
    }

    public static String clearHtml(String content, String tag) {
        StringBuilder html = new StringBuilder();
        Document doc = Jsoup.parse(content);
        Elements elements = doc.select(tag);
        for (int i = 0; i < elements.size(); i++) {
            Element el = elements.get(i);
            html.append(el.html());
        }
        return Jsoup.parse(html.toString()).text();
    }
}

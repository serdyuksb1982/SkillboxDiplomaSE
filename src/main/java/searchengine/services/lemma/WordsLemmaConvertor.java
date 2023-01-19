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
import searchengine.lemma.LemmaHtmlEngin;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repository.PageRepository;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
@Getter
public class WordsLemmaConvertor {

    private final PageRepository pageRepository;

    private final LemmaHtmlEngin lemmaHtmlEngin;

    private List<LemmaDto> lemmaDto;

    public void getConvert(SiteEntity site) {
        lemmaDto = new CopyOnWriteArrayList<>();
        Iterable<PageEntity> pageList = pageRepository.findAll();
        Map<String, Integer> lemmaList = new TreeMap<>();
        for (PageEntity page : pageList) {
            String content = page.getContent();
            String title = clearHtmlCode(content, "title");
            String body = clearHtmlCode(content, "body");
            Map<String, Integer> titleList = lemmaHtmlEngin.getLemmaList(title);
            Map<String, Integer> bodyList = lemmaHtmlEngin.getLemmaList(body);
            Set<String> words = new HashSet<>();
            words.addAll(titleList.keySet());
            words.addAll(bodyList.keySet());
            for (String word : words) {
                int frequency = lemmaList.getOrDefault(word, 0) + 1;
                lemmaList.put(word, frequency);
            }
        }
        for (String lemma : lemmaList.keySet()) {
            Integer frequency = lemmaList.get(lemma);
            LemmaDto lemmaDtoCurrent = new LemmaDto(lemma, frequency);
            lemmaDto.add(lemmaDtoCurrent);

        }

    }

    private String clearHtmlCode(String content, String tag) {
        StringBuffer stringBuffer = new StringBuffer();
        Document document = Jsoup.parse(content);
        Elements elements = document.select(tag);
        for (Element e : elements) {
            stringBuffer.append(e.html());
        }
        return Jsoup.parse(stringBuffer.toString()).text();
    }
}

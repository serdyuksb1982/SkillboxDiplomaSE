package searchengine.services.index;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import searchengine.dto.IndexDto;
import searchengine.lemma.LemmaEngine;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;

import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Getter
@Slf4j
public class WebParser {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final LemmaEngine lemmaEngine;
    private List<IndexDto> indexDtos;
    
    public void startWebParser(SiteModel site) {
        Iterable<PageModel> pageList = pageRepository.findBySiteId(site);
        List<LemmaModel> lemmaList = lemmaRepository.findBySiteModelId(site);
        indexDtos = new ArrayList<>();

        for (PageModel page : pageList) {
            if (page.getCode() < 400) {
                long pageId = page.getId();
                String content = page.getContent();
                String title = clearCodeFromTag(content, "title");
                String body = clearCodeFromTag(content, "body");
                Map<String, Integer> titleList = lemmaEngine.getLemmaMap(title);
                Map<String, Integer> bodyList = lemmaEngine.getLemmaMap(body);

                for (LemmaModel lemma : lemmaList) {
                    long lemmaId = lemma.getId();
                    String keyWord = lemma.getLemma();
                    if (titleList.containsKey(keyWord) || bodyList.containsKey(keyWord)) {
                        float totalRank = 0.0F;
                        if (titleList.get(keyWord) != null) {
                            float titleRank = Float.valueOf(titleList.get(keyWord));
                            totalRank += titleRank;
                        }
                        if (bodyList.get(keyWord) != null) {
                            float bodyRank = (float) (bodyList.get(keyWord) * 0.8);
                            totalRank += bodyRank;
                        }
                        indexDtos.add(new IndexDto(pageId, lemmaId, totalRank));
                    } else {
                        log.debug("Lemma not found");
                    }
                }
            } else {
                log.debug("Bad status code - " + page.getCode());
            }
        }
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
}

package searchengine.services.index;

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
@Slf4j
public class Indexing  {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final LemmaEngine lemmaEngine;
    private List<IndexDto> config;
    
    public void run(SiteModel site) {
        Iterable<PageModel> pageList = pageRepository.findBySiteId(site);
        List<LemmaModel> lemmaList = lemmaRepository.findBySiteModelId(site);
        config = new ArrayList<>();

        for (PageModel page : pageList) {
            if (page.getCode() < 400) {
                long pageId = page.getId();
                String content = page.getContent();
                String title = clearCodeFromTag(content, "title");
                String body = clearCodeFromTag(content, "body");
                Map<String, Integer> titleList = lemmaEngine.getLemmaList(title);
                Map<String, Integer> bodyList = lemmaEngine.getLemmaList(body);

                for (LemmaModel lemma : lemmaList) {
                    Long lemmaId = lemma.getId();
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
                        config.add(new IndexDto(pageId, lemmaId, totalRank));
                    } else {
                        log.debug("Lemma not found");
                    }
                }
            } else {
                log.debug("Bad status code - " + page.getCode());
            }
        }
    }


    public List<IndexDto> getIndexList() {
        return config;
    }

    public  String clearCodeFromTag(String content, String s) {
        StringBuilder stringBuilder = new StringBuilder();
        Document doc = Jsoup.parse(content);
        Elements elements = doc.select(s);
        for (int i = 0; i < elements.size(); i++) {
            Element el = elements.get(i);
            stringBuilder.append(el.html());
        }
        return Jsoup.parse(stringBuilder.toString()).text();
    }
}

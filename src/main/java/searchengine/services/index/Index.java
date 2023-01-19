package searchengine.services.index;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import searchengine.dto.IndexDto;
import searchengine.lemma.LemmaHtmlEngin;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Getter
public class Index {

    private final PageRepository pageRepository;

    private final LemmaRepository lemmaRepository;

    private final LemmaHtmlEngin lemmaHtmlEngin;

    private List<IndexDto> indexDtoList;

    public void call(SiteEntity site) {
        Iterable<PageEntity> pageList = pageRepository.findBySiteId(site);
        List<LemmaEntity> lemmaList = lemmaRepository.findBySiteEntityId(site);
        indexDtoList = new ArrayList<>();

        for (PageEntity page : pageList) {
            if (page.getCode() < 400) {
                long pageId = page.getId();
                String content = page.getContent();
                String title = clearCode(content, "title");
                String body = clearCode(content, "body");
                HashMap<String, Integer> titleList = lemmaHtmlEngin.getLemmaList(title);
                HashMap<String, Integer> bodyList = lemmaHtmlEngin.getLemmaList(body);

                for (LemmaEntity lemma : lemmaList) {
                    Long lemmaId = lemma.getId();
                    String keyWord = lemma.getLemma();
                    if (titleList.containsKey(keyWord) || bodyList.containsKey(keyWord)) {
                        float totalRank = 0.0F;
                        if (titleList.get(keyWord) != null) {
                            Float titleRank = Float.valueOf(titleList.get(keyWord));
                            totalRank += titleRank;
                        }
                        if (bodyList.get(keyWord) != null) {
                            float bodyRank = (float) (bodyList.get(keyWord) * 0.8);
                            totalRank += bodyRank;
                        }
                        indexDtoList.add(new IndexDto(pageId, lemmaId, totalRank));
                    } else {
                        log.debug("Lemma not found");
                    }
                }
            } else {
                log.debug("Bad status code - " + page.getCode());
            }
        }

    }

    public static String clearCode(String content, String tag) {
        StringBuilder html = new StringBuilder();
        var doc = Jsoup.parse(content);
        var elements = doc.select(tag);
        for (Element el : elements) {
            html.append(el.html());
        }
        return Jsoup.parse(html.toString()).text();
    }
}

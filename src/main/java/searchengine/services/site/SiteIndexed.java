package searchengine.services.site;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.IndexDto;
import searchengine.dto.LemmaDto;
import searchengine.dto.PageDto;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.model.enums.Status;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.index.Indexing;
import searchengine.services.lemma.LemmaIndexer;
import searchengine.services.pageconvertor.PageIndexer;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
@Slf4j
public class SiteIndexed implements Runnable {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaIndexer lemmaParser;
    private final Indexing indexParser;
    private final String url;
    private final SitesList sitesList;


    @Override
    public void run() {
        if (siteRepository.findByUrl(url) != null) {
            log.info("Начато удаление данных  сайта - " + url);
            deleteDataFromSite();
        }
        log.info("Начата индексация - " + url + " " + getName());
        saveDateSite();

        try {
            List<PageDto> pageDtoList = getPageDtoList();
            saveToBase(pageDtoList);
            getLemmasPage();
            indexingWords();

        } catch (InterruptedException e) {
            log.error("Индексация остановлена - " + url);
            errorSite();
        }
    }

    private List<PageDto> getPageDtoList() throws InterruptedException {
        if (!Thread.interrupted()) {
            String urlFormat = url + "/";
            List<PageDto> pageDtoVector = new Vector<>();
            List<String> urlList = new Vector<>();
            ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            List<PageDto> pages = forkJoinPool.invoke(new PageIndexer(urlFormat, pageDtoVector, urlList));
            return new CopyOnWriteArrayList<>(pages);
        } else throw new InterruptedException();
    }

    private void saveToBase(List<PageDto> pages) throws InterruptedException {
        if (!Thread.interrupted()) {
            List<PageModel> pageList = new CopyOnWriteArrayList<>();
            SiteModel site = siteRepository.findByUrl(url);

            for (PageDto page : pages) {
                int start = page.getUrl().indexOf(url) + url.length();
                String pageFormat = page.getUrl().substring(start);
                pageList.add(new PageModel(site, pageFormat, page.getCode(),
                        page.getContent()));
            }
            pageRepository.flush();
            pageRepository.saveAll(pageList);
        } else {
            throw new InterruptedException();
        }
    }

    private void getLemmasPage() {
        if (!Thread.interrupted()) {
            SiteModel siteModel = siteRepository.findByUrl(url);
            siteModel.setStatusTime(new Date());
            lemmaParser.run(siteModel);
            List<LemmaDto> lemmaDtoList = lemmaParser.getLemmaDtoList();
            List<LemmaModel> lemmaList = new CopyOnWriteArrayList<>();

            for (LemmaDto lemmaDto : lemmaDtoList) {
                lemmaList.add(new LemmaModel(lemmaDto.getLemma(), lemmaDto.getFrequency(), siteModel));
            }
            lemmaRepository.flush();
            lemmaRepository.saveAll(lemmaList);
        } else {
            throw new RuntimeException();
        }
    }

    private void indexingWords() throws InterruptedException {
        if (!Thread.interrupted()) {
            SiteModel site = siteRepository.findByUrl(url);
            indexParser.run(site);
            List<IndexDto> indexDtoList = new CopyOnWriteArrayList<>(indexParser.getIndexList());
            List<IndexModel> indexList = new CopyOnWriteArrayList<>();
            site.setStatusTime(new Date());
            for (IndexDto indexDto : indexDtoList) {
                PageModel page = pageRepository.getById(indexDto.getPageID());
                LemmaModel lemma = lemmaRepository.getById(indexDto.getLemmaID());
                indexList.add(new IndexModel(page, lemma, indexDto.getRank()));
            }
            indexRepository.flush();
            indexRepository.saveAll(indexList);
            log.info("Индексация завершена - " + url);
            site.setStatusTime(new Date());
            site.setStatus(Status.INDEXED);
            siteRepository.save(site);

        } else {
            throw new InterruptedException();
        }
    }

    private void deleteDataFromSite() {
        SiteModel site = siteRepository.findByUrl(url);
        site.setStatus(Status.INDEXING);
        site.setName(getName());
        site.setStatusTime(new Date());
        siteRepository.save(site);
        siteRepository.flush();
        siteRepository.delete(site);
    }

    private void saveDateSite() {
        SiteModel site = new SiteModel();
        site.setUrl(url);
        site.setName(getName());
        site.setStatus(Status.INDEXING);
        site.setStatusTime(new Date());
        siteRepository.flush();
        siteRepository.save(site);
    }

    private void errorSite() {
        SiteModel site = new SiteModel();
        site.setLastError("Индексация остановлена");
        site.setStatus(Status.FAILED);
        site.setStatusTime(new Date());
        siteRepository.save(site);
    }

    private String getName() {
        List<Site> sites = sitesList.getSites();
        for (Site map : sites) {
            if (map.getUrl().equals(url)) {
                return map.getName();
            }
        }
        return "";
    }
}


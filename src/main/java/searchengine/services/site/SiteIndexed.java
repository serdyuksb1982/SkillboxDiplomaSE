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
            log.info("start site date delete from ".concat(url));
            SiteModel site = siteRepository.findByUrl(url);
            site.setStatus(Status.INDEXING);
            site.setName(getSiteName());
            site.setStatusTime(new Date());
            siteRepository.save(site);
            siteRepository.flush();
            siteRepository.delete(site);
        }
        log.info("Site indexing start ".concat(url).concat(" ").concat(getSiteName()) );
        SiteModel site = new SiteModel();
        site.setUrl(url);
        site.setName(getSiteName());
        site.setStatus(Status.INDEXING);
        site.setStatusTime(new Date());
        siteRepository.flush();
        siteRepository.save(site);
        try {
            if (!Thread.interrupted()) {
                List<PageDto> pageDtoList;
                if (!Thread.interrupted()) {
                    String urlFormat = url.concat("/") ;
                    List<PageDto> pageDtosList = new CopyOnWriteArrayList<>();
                    List<String> urlList = new CopyOnWriteArrayList<>();
                    ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
                    List<PageDto> pages = forkJoinPool.invoke(new PageIndexer(urlFormat, pageDtosList, urlList));
                    pageDtoList = new  CopyOnWriteArrayList<>(pages);
                } else throw new InterruptedException();
                List<PageModel> pageList = new CopyOnWriteArrayList<>();

                for (PageDto page : pageDtoList) {
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
            if (!Thread.interrupted()) {
                indexParser.run(site);
                List<IndexDto> indexDtoList = new CopyOnWriteArrayList<>(indexParser.getIndexList());
                List<IndexModel> indexModels = new CopyOnWriteArrayList<>();
                site.setStatusTime(new Date());
                for (IndexDto indexDto : indexDtoList) {
                    PageModel page = pageRepository.getById(indexDto.getPageID());
                    LemmaModel lemma = lemmaRepository.getById(indexDto.getLemmaID());
                    indexModels.add(new IndexModel(page, lemma, indexDto.getRank()));
                }
                indexRepository.flush();
                indexRepository.saveAll(indexModels);
                log.info("Indexing stopping ".concat(url));
                site.setStatusTime(new Date());
                site.setStatus(Status.INDEXED);
                siteRepository.save(site);

            } else {
                throw new InterruptedException();
            }

        } catch (InterruptedException e) {
            log.error("Indexing stopped from ".concat(url).concat(". ").concat(e.getMessage()));
            SiteModel sites = new SiteModel();
            sites.setLastError("Indexing stopped");
            sites.setStatus(Status.FAILED);
            sites.setStatusTime(new Date());
            siteRepository.save(site);
        }
    }

    private String getSiteName() {
        List<Site> sites = sitesList.getSites();
        for (Site site : sites) {
            if (site.getUrl().equals(url)) {
                return site.getName();
            }
        }
        return "";
    }
}


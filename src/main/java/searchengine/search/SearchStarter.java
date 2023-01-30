package searchengine.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.SearchDto;
import searchengine.model.LemmaModel;
import searchengine.model.SiteModel;
import searchengine.repository.SiteRepository;
import searchengine.services.search.SearchService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchStarter {
    private final SiteRepository siteRepository;

    private final SearchService searchService;

    public List<SearchDto> siteSearch(String text,
                                      String url,
                                      int start,
                                      int limit) {
        SiteModel site = siteRepository.findByUrl(url);
        List<String> textLemmaList = searchService.getLemmaFromSearchText(text);
        List<LemmaModel> foundLemmaList = searchService.getLemmaListFromSite(textLemmaList, site);
        return searchService.createSearchDtoList(foundLemmaList, textLemmaList, start, limit);
    }

    public List<SearchDto> fullSiteSearch(String text,
                                          int start,
                                          int limit) {
        List<SiteModel> siteList = siteRepository.findAll();
        List<SearchDto> result = new ArrayList<>();
        List<LemmaModel> foundLemmaList = new ArrayList<>();
        List<String> textLemmaList = searchService.getLemmaFromSearchText(text);
        for (SiteModel site : siteList) {
            foundLemmaList.addAll(searchService.getLemmaListFromSite(textLemmaList, site));
        }
        List<SearchDto> searchData = new ArrayList<>();
        for (LemmaModel l : foundLemmaList) {
            if (l.getLemma().equals(text)) {
                searchData = (searchService.createSearchDtoList(foundLemmaList, textLemmaList, start, limit));
                searchData.sort(new Comparator<SearchDto>() {
                    @Override
                    public int compare(SearchDto o1, SearchDto o2) {
                        return Float.compare(o2.getRelevance(), o1.getRelevance());
                    }
                });
                if (searchData.size() > limit) {
                    for (int i = start; i < limit; i++) {
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
        return searchData;
    }
}

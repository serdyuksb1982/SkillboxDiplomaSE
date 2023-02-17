package searchengine.search;

import org.springframework.stereotype.Service;
import searchengine.dto.SearchDto;
import searchengine.model.LemmaModel;
import searchengine.model.SiteModel;
import searchengine.repository.SiteRepository;
import searchengine.services.search.SearchService;

import java.util.ArrayList;
import java.util.List;

@Service
public record SearchStarter(SiteRepository siteRepository, SearchService searchService) {

    public List<SearchDto> getSearchFromOneSite(String text,
                                                String url,
                                                int start,
                                                int limit) {
        SiteModel site = siteRepository.findByUrl(url);
        List<String> textLemmaList = searchService.getLemmaFromSearchText(text);
        List<LemmaModel> foundLemmaList = searchService.getLemmaModelFromSite(textLemmaList, site);
        return searchService.createSearchDtoList(foundLemmaList, textLemmaList, start, limit);
    }

    public List<SearchDto> getFullSearch(String text,
                                         int start,
                                         int limit) {
        List<SiteModel> siteList = siteRepository.findAll();
        List<SearchDto> result = new ArrayList<>();
        List<LemmaModel> foundLemmaList = new ArrayList<>();
        List<String> textLemmaList = searchService.getLemmaFromSearchText(text);

        int i = 0;
        while (i < siteList.size()) {
            SiteModel site = siteList.get(i);
            foundLemmaList.addAll(searchService.getLemmaModelFromSite(textLemmaList, site));
            i++;
        }

        List<SearchDto> searchData = new ArrayList<>();
        for (LemmaModel l : foundLemmaList) {
            if (l.getLemma().equals(text)) {
                searchData = (searchService.createSearchDtoList(foundLemmaList, textLemmaList, start, limit));
                searchData.sort((o1, o2) -> Float.compare(o2.relevance(), o1.relevance()));
                if (searchData.size() > limit) {
                    var y = start;
                    while (y < limit) {
                        result.add(searchData.get(y));
                        y++;
                    }
                    return result;
                }
            }
        }
        return searchData;
    }
}

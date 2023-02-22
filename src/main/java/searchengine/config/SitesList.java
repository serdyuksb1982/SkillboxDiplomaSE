package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesList {
    private List<Site> sites;
    //Mozilla/5.0
    private @Value("${search.engine.user.agent}") String userAgent;
    //http://www.google.com
    private @Value("${search.engine.user.referrer}") String referrer;
}

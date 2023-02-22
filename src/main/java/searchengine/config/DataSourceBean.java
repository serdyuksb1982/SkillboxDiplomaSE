package searchengine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceBean {
    //root
    private @Value("${search.engine.db.password}") String dbPassword;
    //root
    private @Value("${search.engine.db.username}") String dbUserName;
    //jdbc:mysql://localhost:3306/search_engine?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    private @Value("${search.engine.db.url}") String dbUrl;

    @Bean
    public DataSource getDataSource() {
        return DataSourceBuilder
                .create()
                .url(dbUrl)
                .username(dbUserName)
                .password(dbPassword)
                .build();
    }
}


# <h1 align="center">Search Engine "SkillBox"</h1>

## This is my graduation project of the JAVA developer course from SkillBox
***

📄<b>_Stack_</b>:
_Java version 17, Spring Boot version 2.5.7, maven, Hibernate, migratiom FlyWay Db, Swagger Api, Lombok,JSOUP,
DB MySQL8O, Morphology Library, create schema -> "charset/Collation: utf8mb4"._

***

## Description
Реализация поискового "движка".
<li>Индексация WEB-сайтов</li>
<li>Получение статистических результатов индексации</li>
<li>Леммизация результатов</li>
<li>Осуществление поиска в индексированном контенте</li>

***

> SpringBoot application. <br>
> The search engine receives sites from the application.yaml. Using ForkJoinPool collects
> information about sites
> it to the MySQL database. RestControllers provides interface to search information


## Файл настройки
- Sites in application.yaml
 indexing-settings:
   sites:
     - url: https://www.playback.ru
       name: PlayBack.Ru
     - url: https://www.lenta.ru
       name: Лента.ру
     - url: https://www.skillbox.ru
       name: Skillbox
     - url: http://redmine-reports.soctech.loc
       name: Soctech.loc

## API Specification

* GET /api/startIndexing  ⌛

> Starts indexing of all sites in specified application.yaml. <br>
> Returns an error if indexing is already running.

 Starts index/reindex webpage given in parameter.
> URL must be related to the domen names given in application.yml.

* GET /api/statistics 📊

## Read this before starting

- Spring Security identification InMemoryUserDetailsManager <br>
  user name: user <br>
  password: password


- Create an empty database **search_engine**
 ``` roomsql
create database search_engine, migrate tables -> flyway;
 ```

- Change the datasource setting in application.yaml.
 ``` yaml

  server:
  port: 8080

spring:
  flyway:
    create-schemas: true
    locations: classpath:db/migration
    enabled: true
    baseline-on-migrate: true

  datasource:
    url: jdbc:mysql://localhost:3306/search_engine?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  mvc:
    path match:
      matching-strategy: ant_path_matcher

indexing-settings:
  sites:
    - url: https://www.skillbox.ru
      name: Skillbox
      #- url: https://www.lenta.ru
      #name: Лента.ру
      #- url: https://www.skillbox.ru
      #name: Skillbox
      #- url: https://www.playback.ru
      #name: PlayBack.Ru
      #- url: http://redmine-reports.soctech.loc
      #name: Soctech.loc
  userAgent: Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6
  referrer: http://www.google.com
search:
  engine:
    db:
      url: ${DB_URL}
      username: ${DB_USER_NAME}
      password: ${DB_PASSWORD}
      
***   
   ENV: DB_PASSWORD=root;
        DB_URL=jdbc:mysql://localhost:3306/search_engine?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true;
        DB_USER_NAME=root
      


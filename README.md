<h1 align="center">Search Engine "SkillBox"</h1>

***

📄<b>_Stack_</b>:
Java version 17, Spring Boot version 2.5.7, maven, Hibernate, migratiom FlyWay Db, Swagger Api, Lombok,JSOUP,
DB MySQL8O, create schema -> "charset/Collation: utf8mb4".

***

## Description

> SpringBoot application. <br>
> The search engine receives sites from the application.yaml. Using ForkJoinPool collects
> information about sites
> it to the MySQL database. RestControllers provides interface to search information

- Sites in application.yaml
 ``` yaml
 indexing-settings:
   sites:
     - url: https://www.playback.ru
       #name: PlayBack.Ru
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

- Create an empty database **search_engine**
 ``` roomsql
create database search_engine, migrate tables -> flyway;
 ```

- Change the datasource setting in application.yaml.
 ``` yaml

  flyway:
    create-schemas: true
    locations: classpath:db/migration
    enabled: true
    baseline-on-migrate: true
  datasource:
    url: jdbc:mysql://localhost:3306/search_engine?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: ${your username}
    password: ${your password}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  From Swagger Api  
  mvc:
    path match:
      matching-strategy: ant_path_matcher
      
   http://localhost:8080/v2/api-docs   
      
***      



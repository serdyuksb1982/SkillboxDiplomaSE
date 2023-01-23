create table hibernate_sequence (next_val bigint)
engine=InnoDB;

insert into hibernate_sequence values ( 1 );

create table lemma (
    id bigint not null auto_increment,
    frequency integer not null,
    lemma varchar(255),
    site_id bigint,
    primary key (id)
) engine=InnoDB;

create table page (
    id bigint not null auto_increment,
    code integer not null,
    content MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci not null,
    path VARCHAR(256) not null,
    site_id bigint not null,
    primary key (id)
) engine=InnoDB;

create table site (
    id bigint not null, last_error varchar(255),
    name varchar(255), status varchar(255),
    status_time datetime(6), url varchar(255),
    primary key (id)
) engine=InnoDB;

create table words_index (
    id bigint not null auto_increment,
    index_rank float not null,
    lemma_id bigint,
    page_id bigint,
    primary key (id)
) engine=InnoDB;




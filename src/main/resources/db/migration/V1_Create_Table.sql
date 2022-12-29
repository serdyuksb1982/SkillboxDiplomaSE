/*create table hibernate_sequence (next_val integer) engine=InnoDB;*/

/*insert into hibernate_sequence values ( 1 );*/

create table page (
    id integer not null AUTO_INCREMENT,
    code integer not null,
    content MEDIUMTEXT not null,
    path VARCHAR(256) not null,
    site_id integer,
    primary key (id)
) engine=InnoDB;


create table site (
    id integer not null AUTO_INCREMENT,
    last_error varchar(255),
    name varchar(255) not null,
    pages integer,
    status integer,
    status_time datetime(6) not null,
    url varchar(255) not null,
    primary key (id)
) engine=InnoDB;

alter table page
    add constraint UK_cue5nl0upbklix5pr26kubo6 unique (path);

alter table page
    add constraint FKj2jx0gqa4h7wg8ls0k3y221h2
        foreign key (site_id) references site (id);
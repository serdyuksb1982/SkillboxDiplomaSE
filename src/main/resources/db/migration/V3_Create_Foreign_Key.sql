alter table lemma
    add constraint FKfbq251d28jauqlxirb1k2cjag foreign key (site_id) references site (id);

alter table page
    add constraint FKj2jx0gqa4h7wg8ls0k3y221h2 foreign key (site_id) references site (id);

alter table words_index
    add constraint FKmnw79vxmxkyy4hjyugjjh34h foreign key (lemma_id) references lemma (id);

alter table words_index
    add constraint FKq9mk1xs3p5e6vbe88o5n4etce foreign key (page_id) references page (id);
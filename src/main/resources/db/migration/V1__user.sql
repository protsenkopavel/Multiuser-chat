create table if not exists nauth_user(name varchar(25) not null unique, pswhash varchar(60) not null, isOnline bool not null default false);

insert into auth_user(name, pswhash) values 
    ('Admin', '$2a$12$Uf69DoqIgmKVdTl8ah5tIOWuTUtueMRaavtl7M9u01oj.7BD5RsG2'),
    ('Operator', '$2a$12$h/bFywkZ8HRJnfKisDygOO3XQsWxnv052qxZ72UPj78DBW3mxEa5a');
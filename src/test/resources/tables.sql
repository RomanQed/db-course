create table currencies (
    id   integer auto_increment primary key,
    name varchar(256)
);

create table users (
    id       integer auto_increment primary key,
    login    varchar(256),
    password varchar(256),
    admin    bool
);

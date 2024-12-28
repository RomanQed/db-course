create table currencies
(
    id   serial not null primary key,
    name text not null
);

create table exchanges
(
    id     serial not null primary key,
    _from  int references currencies (id) on delete cascade not null,
    _to    int references currencies (id) on delete cascade not null,
    factor double precision not null
);

create table categories
(
    id   serial not null primary key,
    name text not null
);

create table users
(
    id       serial not null primary key,
    login    text not null,
    password text not null,
    admin    bool not null
);

create table budgets
(
    id          serial not null primary key,
    owner       int references users (id) on delete cascade not null,
    currency    int references currencies (id) on delete cascade not null,
    _start      timestamp not null,
    _end        timestamp not null,
    description text      not null,
    value       double precision not null
);

create table accounts
(
    id          serial not null primary key,
    owner       int references users (id) on delete cascade not null,
    currency    int references currencies (id) on delete cascade not null,
    description text not null,
    value       double precision not null
);

create table transactions
(
    id          serial not null primary key,
    owner       int references users (id) on delete cascade not null,
    category    int references categories (id) on delete cascade not null,
    _from       int references accounts (id) on delete cascade,
    _to         int references accounts (id) on delete cascade,
    value       double precision not null,
    description text      not null,
    _timestamp  timestamp not null
);
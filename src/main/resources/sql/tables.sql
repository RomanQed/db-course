create table currencies
(
    id   serial primary key,
    name text not null
);

create table exchanges
(
    id     serial primary key,
    _from  int references currencies (id) on delete cascade,
    _to    int references currencies (id) on delete cascade,
    factor double precision
);

create table categories
(
    id   serial primary key,
    name text not null
);

create table users
(
    id       serial primary key,
    login    text not null,
    password text not null,
    admin    bool not null
);

create table budgets
(
    id          serial primary key,
    owner       int references currencies (id) on delete cascade,
    currency    int references currencies (id) on delete cascade,
    _start      timestamp not null,
    _end        timestamp not null,
    description text      not null,
    value       double precision
);

create table accounts
(
    id          serial primary key,
    owner       int references users (id) on delete cascade,
    currency    int references currencies (id) on delete cascade,
    description text not null,
    value       double precision
);

create table goals
(
    id          serial primary key,
    owner       int references users (id) on delete cascade,
    account     int references accounts (id) on delete cascade,
    description text not null,
    target      double precision
);

create table transactions
(
    id          serial primary key,
    owner       int references users (id) on delete cascade,
    category    int references categories (id) on delete cascade,
    _from       int references accounts (id) on delete cascade,
    _to         int references accounts (id) on delete cascade,
    value       double precision,
    description text      not null,
    _timestamp  timestamp not null
);
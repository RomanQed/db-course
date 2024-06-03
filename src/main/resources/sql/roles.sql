drop role if exists _service;
drop role if exists _user;
drop role if exists _admin;

create role _service;
grant all privileges on database %database to _service;
grant all privileges on currencies to _service;
grant all privileges on exchanges to _service;
grant all privileges on categories to _service;
grant all privileges on users to _service;
grant all privileges on budgets to _service;
grant all privileges on accounts to _service;
grant all privileges on goals to _service;
grant all privileges on transactions to _service;
grant usage, select on all sequences in schema public to _service;

create role _user;
grant select on currencies to _user;
grant select on exchanges to _user;
grant select on categories to _user;
grant select on users to _user;
grant update on users to _user;
grant all privileges on budgets to _user;
grant all privileges on accounts to _user;
grant all privileges on goals to _user;
grant all privileges on transactions to _user;
grant usage, select on all sequences in schema public to _user;

create role _admin;
grant all privileges on currencies to _admin;
grant all privileges on exchanges to _admin;
grant all privileges on categories to _admin;
grant select on users to _admin;
grant update on users to _admin;
grant all privileges on budgets to _admin;
grant all privileges on accounts to _admin;
grant all privileges on goals to _admin;
grant all privileges on transactions to _admin;
grant usage, select on all sequences in schema public to _admin;

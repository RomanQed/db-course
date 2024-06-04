create table t_part
(check (_timestamp  between '%start'::timestamp and '%end'::timestamp))
inherits (transactions);

create table t_part_others
(check (_timestamp not between '%start'::timestamp and '%end'::timestamp))
    inherits (transactions);

create function transactions_insert_trigger()
returns trigger as $$
begin
    if (new._timestamp between '%start'::timestamp and '%end'::timestamp) then
       insert into t_part values (new.*);
    else
       insert into t_part_others values (new.*);
    end if;
    return null;
end;
$$
language plpgsql;

create trigger insert_transaction
before insert on transactions
for each row execute function transactions_insert_trigger();
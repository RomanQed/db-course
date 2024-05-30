create
or replace function check_table_exists(in tbl text)
returns bool as $$
begin
return (select exists (select * from information_schema.tables where table_name = tbl and table_schema = 'public'));
end;
$$
language plpgsql;

create
or replace function check_tables_exist(in tbls text[])
returns bool as $$
declare
e text;
begin
	foreach
e in array tbls
	loop
		if not check_table_exists(e) then
			return false;
end if;
end loop;
return true;
end;
$$
language plpgsql;
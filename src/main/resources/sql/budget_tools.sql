create function convert_value(in _f int, in _t int, in _value double precision)
returns double precision as $$
declare
    _factor double precision;
begin
	if _f = _t then
		return _value;
    end if;
	_factor = (select factor from exchanges where _from = _f and _to = _t);
    return _value * _factor;
end;
$$
language plpgsql;

create type budget_status as (spent double precision, got double precision, total double precision);

create function get_budget_status(in _id int, in _user int)
returns budget_status as $$
declare
    _t record;
	_from int;
	_to int;
	_value double precision;
	_spent double precision;
	_got double precision;
	_b_cur int;
	_cur int;
	_s timestamp;
	_e timestamp;
begin
	_s = (select _start from budgets where id = _id);
	_e = (select _end from budgets where id = _id);
	_value = (select value from budgets where id = _id);
	_b_cur = (select currency from budgets where id = _id);
	_got = 0;
	_spent = 0;
    for _t in (select * from transactions where _timestamp between _s and _e)
	loop
		_from = _t._from;
		_to = _t._to;
		-- Skip inner transactions
		if (_from is not null) and (_to is not null) then
			continue;
        end if;
		-- Spent
		if (_to is null) then
			_cur = (select currency from accounts where id = _t._from);
			_spent = _spent + (select convert_value(_cur, _b_cur, _t.value));
            continue;
        end if;
		-- Got
		_cur = (select currency from accounts where id = _t._to);
		_got = _got + (select convert_value(_cur, _b_cur, _t.value));
    end loop;
    return (select cast(row(_spent, _got, _value + _got - _spent) as budget_status));
end;
$$
language plpgsql;
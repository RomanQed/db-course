create procedure add_account_value(
	in _id int,
	in _currency int,
	in _value double precision
) as $$
declare
    _factor double precision;
	_t_cur int;
begin
	_t_cur = (select currency from accounts where id = _id);
	if _t_cur = _currency then
        update accounts
        set value = value + _value
        where id = _id;
        return;
    end if;
	_factor = (select factor from exchanges where _from = _currency and _to = _t_cur);
    update accounts
    set value = value + _value * _factor
    where id = _id;
end;
$$
language plpgsql;

create function add_transaction(
	in _owner int,
	in _category int,
	in _from int,
	in _to int,
	in _value double precision,
	in _description text,
	in _timestamp timestamp
)
returns int as $$
declare
    _id int;
	_currency int;
begin
    insert into transactions
    (owner, category, _from, _to, value, description, "_timestamp")
    values (_owner, _category, _from, _to, _value, _description, _timestamp);
    _id = (select currval('transactions_id_seq'));
	-- If from is not null and to is not null, then use _from.currency as main currency
	if (_from is not null) and (_to is not null) then
		_currency = (select currency from accounts where id = _from);
        update accounts
        set value = value - _value
        where id = _from;
        call add_account_value(_to, _currency, _value);
        return _id;
    end if;
	-- If from is null and to is not null, then just update to with value
	if (_from is null) and (_to is not null) then
        update accounts
        set value = value + _value
        where id = _to;
        return _id;
    end if;
	-- If from is not null and to is null, then just update from with -value
    update accounts
    set value = value - _value
    where id = _from;
    return _id;
end;
$$
language plpgsql;

create procedure del_transaction(in _id int)
as $$
declare
    _f int;
	_t int;
	_currency int;
	_v double precision;
begin
	_f = (select _from from transactions where id = _id);
	_t = (select _to from transactions where id = _id);
	_v = (select value from transactions where id = _id);
    delete from transactions where id = _id;
    -- If from is not null and to is not null, then use _from.currency as main currency
    if (_f is not null) and (_t is not null) then
		_currency = (select currency from accounts where id = _f);
        update accounts
        set value = value + _v
        where id = _f;
        call add_account_value(_t, _currency, -_v);
        return;
    end if;
	-- If from is null and to is not null, then sub value from to
	if (_f is null) and (_t is not null) then
        update accounts
        set value = value - _v
        where id = _t;
        return;
    end if;
	-- If from is not null and to is null, then add value to from
    update accounts
    set value = value + _v
    where id = _f;
end;
$$
language plpgsql;
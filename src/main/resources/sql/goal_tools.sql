create type goal_status as (percents double precision, reached double precision, remained double precision);

create function get_goal_status(in _id int)
returns goal_status as $$
declare
    _acc_id int;
	_goal double precision;
	_acc double precision;
	_delta double precision;
begin
	_goal = (select target from goals where id = _id);
	_acc_id = (select account from goals where id = _id);
	_acc = (select value from accounts where id = _acc_id);
	_delta = _goal - _acc;
	if (_delta > 0) then
		return (select cast(row(_acc / _goal * 100, _acc, _delta) as goal_status));
    end if;
    return (select cast(row(100, _goal, 0) as goal_status));
end;
$$
language plpgsql;
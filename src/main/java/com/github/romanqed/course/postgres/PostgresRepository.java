package com.github.romanqed.course.postgres;

import com.github.romanqed.course.database.Repository;
import com.github.romanqed.course.models.Entity;
import com.github.romanqed.jfunc.Exceptions;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

final class PostgresRepository<V extends Entity> implements Repository<V> {
    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    private final Connection connection;
    private final String table;
    private final Supplier<V> supplier;
    private final Serializer serializer;
    private final Deserializer deserializer;

    PostgresRepository(Connection connection,
                       String table,
                       Supplier<V> supplier,
                       Serializer serializer,
                       Deserializer deserializer) {
        this.connection = connection;
        this.table = table;
        this.supplier = supplier;
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    private String toString(Object value) {
        if (value == null) {
            return "null";
        }
        var type = value.getClass();
        if (type == String.class) {
            return "'" + value + "'";
        }
        if (type == Date.class) {
            return "'" + FORMATTER.format(value) + "'";
        }
        return value.toString();
    }

    private String prepareInsert(String role, Map<String, Object> fields) {
        var query = "set role %s;insert into %s (%s) values (%s);";
        // Prepare names
        var names = fields
                .keySet()
                .stream()
                .reduce("", (p, n) -> p + "," + n)
                .substring(1);
        var values = fields
                .values()
                .stream()
                .map(this::toString)
                .reduce("", (p, n) -> p + "," + n)
                .substring(1);
        var sql = String.format(query, role, table, names, values);
        return sql + String.format("select currval('%s_id_seq');", table);
    }

    private int innerPut(String role, Map<String, Object> fields) throws SQLException {
        var sql = prepareInsert(role, fields);
        var statement = connection.createStatement();
        statement.execute(sql);
        statement.getMoreResults(); // Skip set role
        statement.getMoreResults(); // Skip update
        var set = statement.getResultSet();
        if (!set.next()) {
            throw new IllegalStateException("Cannot retrieve id");
        }
        var ret = set.getInt(1);
        statement.close();
        return ret;
    }

    @Override
    public void put(String role, V model) {
        var fields = new HashMap<String, Object>();
        serializer.serialize(fields::put, model);
        var id = Exceptions.suppress(() -> innerPut(role, fields));
        model.setId(id);
    }

    private String prepareUpdate(String role, int id, Map<String, Object> fields) {
        var ret = "set role %s;update %s set %s where id = %d;";
        var builder = new StringBuilder();
        fields.forEach((k, v) -> builder
                .append(',')
                .append(k)
                .append('=')
                .append(toString(v)));
        var values = builder.substring(1);
        return String.format(ret, role, table, values, id);
    }

    private void innerUpdate(String role, int id, Map<String, Object> fields) throws SQLException {
        var sql = prepareUpdate(role, id, fields);
        var statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    @Override
    public void update(String role, V model) {
        var fields = new HashMap<String, Object>();
        serializer.serialize(fields::put, model);
        Exceptions.suppress(() -> innerUpdate(role, model.getId(), fields));
    }

    private String prepareGet(String role, String where) {
        var ret = String.format("set role %s;select * from %s", role, table);
        if (where != null) {
            return ret + " where " + where;
        }
        return ret;
    }

    private List<V> innerGet(String sql) throws SQLException {
        var statement = connection.createStatement();
        statement.execute(sql);
        statement.getMoreResults(); // Skip set role
        var ret = new LinkedList<V>();
        var set = statement.getResultSet();
        while (set.next()) {
            var model = supplier.get();
            var getter = (Getter) (name, type) -> Exceptions.suppress(() -> set.getObject(name, type));
            deserializer.deserialize(getter, model);
            ret.add(model);
        }
        statement.close();
        return ret;
    }

    @Override
    public V get(String role, int key) {
        var found = Exceptions.suppress(() -> innerGet(prepareGet(role, "id = " + key)));
        if (found.isEmpty()) {
            return null;
        }
        return found.get(0);
    }

    @Override
    public List<V> get(String role, String where) {
        return Exceptions.suppress(() -> innerGet(prepareGet(role, where)));
    }

    @Override
    public List<V> get(String role, String field, Object value) {
        return Exceptions.suppress(() -> innerGet(prepareGet(role, field + " = " + toString(value))));
    }

    @Override
    public V getFirst(String role, String field, Object value) {
        var ret = Exceptions.suppress(() ->
                innerGet(prepareGet(role, field + " = " + toString(value) + " limit 1")));
        if (ret.isEmpty()) {
            return null;
        }
        return ret.get(0);
    }

    @Override
    public List<V> get(String role) {
        return Exceptions.suppress(() -> innerGet(prepareGet(role, null)));
    }

    private String prepareExists(String role, String where) {
        var ret = "set role %s;select exists(select 1 from %s where %s)";
        return String.format(ret, role, table, where);
    }

    private boolean innerExists(String sql) throws SQLException {
        var statement = connection.createStatement();
        statement.execute(sql);
        statement.getMoreResults(); // Skip set role
        var set = statement.getResultSet();
        if (!set.next()) {
            throw new IllegalStateException("Cannot retrieve exist state");
        }
        var ret = set.getBoolean(1);
        statement.close();
        return ret;
    }

    @Override
    public boolean exists(String role, int id) {
        return Exceptions.suppress(() -> innerExists(prepareExists(role, "id = " + id)));
    }

    @Override
    public boolean exists(String role, String where) {
        return Exceptions.suppress(() -> innerExists(prepareExists(role, where)));
    }

    @Override
    public boolean exists(String role, String field, Object value) {
        return exists(role, field + " = " + toString(value));
    }

    private String prepareDelete(String role, String where) {
        var ret = String.format("set role %s; delete from %s", role, table);
        if (where != null) {
            return ret + " where " + where;
        }
        return ret;
    }

    private void innerDelete(String sql) throws SQLException {
        var statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    @Override
    public void delete(String role, int key) {
        Exceptions.suppress(() -> innerDelete(prepareDelete(role, "id = " + key)));
    }

    @Override
    public void delete(String role, String where) {
        Exceptions.suppress(() -> innerDelete(prepareDelete(role, where)));
    }
}

package io.simplequerybuilder;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseQueryBuilder<T extends BaseQueryBuilder<T>> extends Spec {
    protected final List<String> columns = new ArrayList<>();
    private final Set<String> orderBy = new LinkedHashSet<>();
    private final LinkedHashSet<String> defaultSorts = new LinkedHashSet<>();
    private final List<String> groupBy = new ArrayList<>();
    private final List<String> having = new ArrayList<>();
    private String from = "";
    protected int limit;
    protected long offset;

    protected abstract T self();

    public T orderBy(String... orderByColumns) {
        return orderBy(List.of(orderByColumns));
    }

    public T defaultOrderBy(String... orderByColumns) {
        var trimmedColumns = Stream.of(orderByColumns)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        defaultSorts.addAll(trimmedColumns);
        return self();
    }

    public T orderBy(Collection<String> orderByColumns) {
        var trimmedColumns = orderByColumns.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        orderBy.addAll(trimmedColumns);
        return self();
    }


    public T limit(int limit) {
        this.limit = limit;
        return self();
    }

    public T offset(long offset) {
        this.offset = offset;
        return self();
    }

    public T from(String from) {
        this.from = from;
        return self();
    }

    public T fromSubQuery(BaseQueryBuilder<?> subQuery, String alias) {
        from("(%s) %s".formatted(subQuery.buildSql(), alias));
        return self();
    }

    public T innerJoin(BaseQueryBuilder<?> subQuery, String alias) {
        innerJoin("(%s) %s".formatted(subQuery.buildSql(), alias));
        return self();
    }

    public T innerJoinLateral(BaseQueryBuilder<?> subQuery, String alias) {
        innerJoin("lateral (%s) %s on true".formatted(subQuery.buildSql(), alias));
        return self();
    }

    public T leftJoin(BaseQueryBuilder<?> subQuery, String alias) {
        leftJoin("(%s) %s".formatted(subQuery.buildSql(), alias));
        return self();
    }

    public T leftJoinLateral(BaseQueryBuilder<?> subQuery, String alias) {
        leftJoin("lateral (%s) %s on true".formatted(subQuery.buildSql(), alias));
        return self();
    }

    public T select(String column) {
        columns.add(column);
        return self();
    }

    public T select(String... columns) {
        for (String column : columns)
            select(column);
        return self();
    }

    @Override
    public T join(String joinStatement) {
        super.join(joinStatement);
        return self();
    }

    @Override
    public T innerJoin(String joinStatement) {
        super.innerJoin(joinStatement);
        return self();
    }

    @Override
    public T leftJoin(String joinStatement) {
        super.leftJoin(joinStatement);
        return self();
    }

    public T groupBy(String... groupByColumns) {
        groupBy.addAll(Arrays.asList(groupByColumns));
        return self();
    }

    public T having(String... havingCriteria) {
        this.having.addAll(Arrays.asList(havingCriteria));
        return self();
    }

    public T andIf(boolean condition, Spec other) {
        if (condition)
            super.and(other);
        return self();
    }

    public T andIf(boolean condition, String filter) {
        if (condition)
            and(filter);
        return self();
    }

    @Override
    public T and(Spec other) {
        super.and(other);
        return self();
    }

    @Override
    public T and(String filter) {
        super.and(filter);
        return self();
    }

    @Override
    public T where(String filter) {
        super.where(filter);
        return self();
    }

    public T where(Spec filter) {
        super.and(filter);
        return self();
    }

    @Override
    public T or(String filter) {
        super.or(filter);
        return self();
    }

    @Override
    public T or(Spec other) {
        super.or(other);
        return self();
    }

    @Override
    public T append(String filter) {
        super.append(filter);
        return self();
    }


    public String buildCountSql() {
        return buildSql(true, true);
    }

    public String buildSql() {
        return buildSql(false, true);
    }

    protected String buildSql(boolean countQuery, boolean includePaging) {

        var sql = new StringBuilder("select ");
        if (countQuery)
            sql.append("count(1) ");
        else
            sql.append(String.join(", ", columns));

        sql.append(" from ").append(from);
        if (!joins.isEmpty())
            sql.append(" ").append(String.join(" ", joins));

        if (!where.isEmpty())
            sql.append(buildWhereStatement());

        if (!groupBy.isEmpty())
            sql.append(" group by ").append(String.join(", ", groupBy));

        if (!having.isEmpty())
            sql.append(" having ").append(String.join(", ", having));

        if (!countQuery) {
            if (!orderBy.isEmpty())
                sql.append(" order by ").append(String.join(", ", orderBy));
            else if (!defaultSorts.isEmpty())
                sql.append(" order by ").append(String.join(", ", defaultSorts));

            if (includePaging) {
                if (limit > 0)
                    sql.append(" limit ").append(limit);

                if (offset > 0)
                    sql.append(" offset ").append(offset);
            }
        }

        return sql.toString();
    }

    public String buildWhereStatement() {
        var escaped = buildFilterStatements();
        if (escaped == null)
            return "";
        return " where" + escaped;
    }

    public T andIn(String column, BaseQueryBuilder<T> inQuery) {
        and("%s in (%s)".formatted(column, inQuery.buildSql()));
        return self();
    }

    public T orIn(String column, BaseQueryBuilder<T> inQuery) {
        or("%s in (%s)".formatted(column, inQuery.buildSql()));
        return self();
    }
}

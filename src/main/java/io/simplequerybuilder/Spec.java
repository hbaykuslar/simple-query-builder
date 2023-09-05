package io.simplequerybuilder;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class Spec {

    protected final Set<String> where = new LinkedHashSet<>();
    protected final Set<String> joins = new LinkedHashSet<>();


    public Spec join(String joinStatement) {
        joins.add(joinStatement.trim());
        return this;
    }

    public Spec innerJoin(String joinStatement) {
        joins.add("inner join " + joinStatement.trim());
        return this;
    }

    public Spec leftJoin(String joinStatement) {
        joins.add("left join " + joinStatement.trim());
        return this;
    }

    public Spec and(String filter) {
        where.add("and %s".formatted(filter));
        return this;
    }

    public Spec where(String filter) {
        where.add("and %s".formatted(filter.trim()));
        return this;
    }

    public Spec or(String filter) {
        where.add("or %s".formatted(filter.trim()));
        return this;
    }

    public Spec and(Spec spec) {
        mergeSpecs(spec);
        if (!spec.where.isEmpty())
            and("(%s)".formatted(spec.buildFilterStatements().trim()));
        return this;
    }

    public Spec append(String filter) {
        where.add(filter.trim());
        return this;
    }

    private void mergeSpecs(Spec spec) {
        joins.addAll(spec.joins);
    }


    public String buildFilterStatements() {
        return buildFilterStatements(where);
    }

    protected String buildFilterStatements(Collection<String> criteria) {
        String escaped = null;
        var filters = String.join(" ", criteria);
        if (filters.startsWith("and "))
            escaped = " " + filters.substring("and ".length());

        if (filters.startsWith("or "))
            escaped = " " + filters.substring("or ".length());

        if (escaped == null)
            return null;

        return escaped;
    }

    protected Spec or(Spec spec) {
        mergeSpecs(spec);
        if (!spec.where.isEmpty())
            or("(%s)".formatted(spec.buildFilterStatements()));
        return this;
    }

}

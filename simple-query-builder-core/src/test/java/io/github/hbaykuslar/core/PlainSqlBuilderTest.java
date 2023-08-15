package io.github.hbaykuslar.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlainSqlBuilderTest {

    @Test
    void should_create_select_all_query() {
        var sql = new PlainSqlBuilder()
                .select("o.*")
                .from("orders o")
                .buildSql();

        assertThat(sql).isEqualTo("select o.* from orders o");
    }

    @Test
    void should_create_select_with_where_query() {
        var sql = new PlainSqlBuilder()
                .select("o.*")
                .from("orders o")
                .where("o.id = :orderId")
                .buildSql();

        assertThat(sql).isEqualTo("select o.* from orders o where o.id = :orderId");
    }

    @Test
    void should_create_select_with_where_in_query() {
        var topCustomersBuilder = new PlainSqlBuilder()
                .select("expensiveOrder.customer_id")
                .from("orders expensiveOrder")
                .orderBy("expensiveOrder.amount desc")
                .limit(3);

        var sql = new PlainSqlBuilder()
                .select("o.*")
                .from("orders o")
                .where("o.id = :orderId")
                .andIn("o.customer_id", topCustomersBuilder)
                .buildSql();

        String expected = """
                select o.* from orders o where o.id = :orderId and \
                o.customer_id in \
                (select expensiveOrder.customer_id from orders expensiveOrder order by expensiveOrder.amount desc limit 3)""";
        assertThat(sql).isEqualTo(expected);
    }

    @Test
    void should_create_select_with_where_multiple_criteria_query() {
        var sql = new PlainSqlBuilder()
                .select("o.*")
                .from("orders o")
                .where("o.id = :orderId")
                .and("o.created_date > :startDate")
                .or("o.created_date <= :endDate")
                .buildSql();

        var expected = """
            select o.* \
            from orders o \
            where o.id = :orderId and o.created_date > :startDate or o.created_date <= :endDate""";
        assertThat(sql).isEqualTo(expected);
    }

    @Test
    void should_create_select_with_where_multiple_criteria_and_order_by_query() {
        var sql = new PlainSqlBuilder()
                .select("o.*")
                .from("orders o")
                .where("o.id = :orderId")
                .and("o.created_date > :startDate")
                .or("o.created_date <= :endDate")
                .orderBy("o.created_date desc", "o.name asc")
                .buildSql();

        var expected = """
            select o.* \
            from orders o \
            where o.id = :orderId and o.created_date > :startDate or o.created_date <= :endDate \
            order by o.created_date desc, o.name asc""";
        assertThat(sql).isEqualTo(expected);
    }

    @Test
    void should_create_select_with_spec_query() {

        var dateBetweenSpec = new Spec()
                .where("o.created_date > :startDate")
                .or("o.created_date <= :endDate");

        var sql = new PlainSqlBuilder()
                .select("o.*")
                .from("orders o")
                .where(dateBetweenSpec)
                .and("o.id = :orderId")
                .orderBy("o.created_date desc", "o.name asc")
                .buildSql();

        var expected = """
            select o.* \
            from orders o \
            where (o.created_date > :startDate or o.created_date <= :endDate) and o.id = :orderId \
            order by o.created_date desc, o.name asc""";
        assertThat(sql).isEqualTo(expected);
    }

    @Test
    void should_create_select_with_where_multiple_criteria_and_order_by_and_groupby_having_query() {
        var sql = new PlainSqlBuilder()
                .select("o.name", "count(1)")
                .from("orders o")
                .where("o.created_date > :startDate")
                .or("o.created_date <= :endDate")
                .groupBy("o.name")
                .having("count(1) > 2")
                .orderBy("o.name asc")
                .buildSql();

        var expected = """
            select o.name, count(1) \
            from orders o \
            where o.created_date > :startDate or o.created_date <= :endDate \
            group by o.name \
            having count(1) > 2 \
            order by o.name asc""";
        assertThat(sql).isEqualTo(expected);
    }

}
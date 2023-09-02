package io.github.hbaykuslar.core;

import io.github.hbaykuslar.core.utils.TestUtils;
import org.junit.jupiter.api.Test;

import static io.github.hbaykuslar.core.utils.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;

class PlainSqlBuilderTest {

    private final PlainSqlBuilder baseQuery = new PlainSqlBuilder()
            .select("o.*")
            .from("order o");
    private static final String baseExpectedSqlString = "select o.* from order o ";

    @Test
    void should_set_offset() {
        var query = baseQuery
                .limit(5)
                .offset(4)
                .buildSql();
        var expectedString = baseExpectedSqlString + "limit 5 offset 4";

        assertThat(query).isEqualTo(expectedString);
    }

    @Test
    void should_create_leftJoin_criteria() {
        var query = baseQuery.leftJoin("agency a on o.agency_id = a.id")
                .buildSql();

        var expectedString = baseExpectedSqlString + "left join agency a on o.agency_id = a.id";

        assertThat(query).isEqualTo(expectedString);
    }

    @Test
    void should_create_count_query() {
        var expectedString = """
                select count(1)  \
                from agency a \
                where a.id = :accountId\
                """;
        var query = new PlainSqlBuilder()
                .from("agency a")
                .where("a.id = :accountId")
                .buildCountSql();
        assertThat(query).isEqualTo(expectedString);
    }

    @Test
    void should_create_innerJoin_criteria() {
        var query = baseQuery.innerJoin("agency a on o.agency_id = a.id")
                .buildSql();

        var expectedString = baseExpectedSqlString + "inner join agency a on o.agency_id = a.id";

        assertThat(query).isEqualTo(expectedString);
    }

    @Test
    void should_create_a_leftJoin_criteria_from_subQuery() {
        var subQuery = new PlainSqlBuilder()
                .select("a.id as accountId, a.name as accountName, u.name as userName, u.email")
                .from("account a")
                .innerJoin("users u on a.user_id = u.id");
        var query = new PlainSqlBuilder()
                .select("o.id, a.accountName, a.userName")
                .from("order o")
                .leftJoin(subQuery, "a")
                .buildSql();
        var expectedString = """
                select
                    o.id,
                    a.accountName,
                    a.userName
                from order o
                    left join (select
                                    a.id as accountId,
                                    a.name as accountName,
                                    u.name as userName,
                                    u.email
                               from account a
                                    inner join users u on a.user_id = u.id) a\
                """;
        assertThat(query).isEqualTo(inlined(expectedString));
    }

    @Test
    void should_create_an_innerJoin_criteria_from_subQuery() {
        var subQuery = new PlainSqlBuilder()
                .select("a.id as accountId, a.name as accountName, u.name as userName, u.email")
                .from("account a")
                .innerJoin("users u on a.user_id = u.id");
        var query = new PlainSqlBuilder()
                .select("o.id, a.accountName, a.userName")
                .from("order o")
                .innerJoin(subQuery, "a")
                .buildSql();
        var expectedString = """
                select o.id, a.accountName, a.userName
                from order o
                    inner join (select
                                    a.id as accountId,
                                    a.name as accountName,
                                    u.name as userName,
                                    u.email
                                from account a
                                    inner join users u on a.user_id = u.id) a\
                """;
        assertThat(query).isEqualTo(inlined(expectedString));
    }

    @Test
    void should_create_an_innerJoinLateral_criteria() {
        var subQuery = new PlainSqlBuilder()
                .select("a.id as accountId, a.name as accountName, u.name as userName, u.email")
                .from("account a")
                .innerJoin("users u on a.user_id = u.id")
                .where("o.account_id = a.id");
        var query = new PlainSqlBuilder()
                .select("o.id, a.accountName, a.userName")
                .from("order o")
                .innerJoinLateral(subQuery, "a")
                .buildSql();

        var expectedString = """
                select
                    o.id,
                    a.accountName,
                    a.userName
                from order o
                    inner join lateral (select
                                            a.id as accountId,
                                            a.name as accountName,
                                            u.name as userName,
                                            u.email
                                        from account a
                                            inner join users u on a.user_id = u.id
                                        where o.account_id = a.id) a on true\
                """;
        assertThat(query).isEqualTo(inlined(expectedString));
    }

    @Test
    void should_create_a_leftJoinLateral_criteria() {
        var subQuery = new PlainSqlBuilder()
                .select("a.id as accountId, a.name as accountName, u.name as userName, u.email")
                .from("account a")
                .innerJoin("users u on a.user_id = u.id")
                .where("o.account_id = a.id");
        var query = new PlainSqlBuilder()
                .select("o.id, a.accountName, a.userName")
                .from("order o")
                .leftJoinLateral(subQuery, "a")
                .buildSql();

        var expectedString = """
                select
                    o.id,
                    a.accountName,
                    a.userName
                from order o
                    left join lateral (select
                                            a.id as accountId,
                                            a.name as accountName,
                                            u.name as userName,
                                            u.email
                                        from account a
                                            inner join users u on a.user_id = u.id
                                        where o.account_id = a.id) a on true\
                """;
        assertThat(query).isEqualTo(inlined(expectedString));
    }

    @Test
    void should_query_from_subQuery() {
        var subQuery = new PlainSqlBuilder()
                .select("a.name as accountName, u.name as userName, u.email")
                .from("account a")
                .innerJoin("users u on a.user_id = u.id");
        var query = new PlainSqlBuilder()
                .select("a.accountName, a.email")
                .fromSubQuery(subQuery, "a")
                .buildSql();
        var expectedString = """
                select
                    a.accountName,
                    a.email
                from (select
                        a.name as accountName,
                        u.name as userName,
                        u.email
                       from account a
                        inner join users u on a.user_id = u.id) a\
                """;
        assertThat(query).isEqualTo(inlined(expectedString));
    }

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
                select o.*
                from orders o
                where o.id = :orderId
                    and o.customer_id in (select
                                            expensiveOrder.customer_id
                                           from orders expensiveOrder
                                           order by expensiveOrder.amount desc
                                           limit 3)""";
        assertThat(sql).isEqualTo(inlined(expected));
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
                select o.*
                from orders o
                where o.id = :orderId
                    and o.created_date > :startDate
                    or o.created_date <= :endDate""";
        assertThat(sql).isEqualTo(inlined(expected));
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
                select o.*
                from orders o
                where o.id = :orderId
                    and o.created_date > :startDate
                    or o.created_date <= :endDate
                order by o.created_date desc, o.name asc""";
        assertThat(sql).isEqualTo(inlined(expected));
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
                select o.*
                from orders o
                where (o.created_date > :startDate or o.created_date <= :endDate)
                       and o.id = :orderId
                order by o.created_date desc, o.name asc""";
        assertThat(sql).isEqualTo(inlined(expected));
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
                select o.name, count(1)
                from orders o
                where o.created_date > :startDate
                    or o.created_date <= :endDate
                group by o.name
                    having count(1) > 2
                order by o.name asc""";
        assertThat(sql).isEqualTo(inlined(expected));
    }

    @Test
    void should_add_and_conditions_correctly() {
        boolean correct = true;
        boolean incorrect = false;
        var query = new PlainSqlBuilder()
                .select("a.*, o.*, u.*")
                .from("order o")
                .innerJoin("account a on o.account_id = a.id")
                .leftJoin("user u on u.id = a.user_id")
                .where("a.id = :accountId")
                .andIf(correct, "o.id = :orderId")
                .andIf(incorrect, "u.id = :userId")
                .buildSql();
        var expectedQuery = """
                select a.*, o.*, u.*
                from order o
                    inner join account a on o.account_id = a.id
                    left join user u on u.id = a.user_id
                where a.id = :accountId
                    and o.id = :orderId
                """;
        assertThat(query).isEqualTo(inlined(expectedQuery));
    }

    @Test
    void query_shouldnt_change_when_order_changes() {
        var firstQuery = new PlainSqlBuilder()
                .select("a.*,b.*,c.*")
                .from("account a")
                .innerJoin("blocked b on ")
                .leftJoin("channel c")
                .where("a.id = :accountId")
                .and("c.id = :chanelId")
                .or("b.blocked is false")
                .append("a.id = c.account_id or b.blocked is false")
                .buildSql();
        var secondQuery = new PlainSqlBuilder()
                .where("a.id = :accountId")
                .and("c.id = :chanelId")
                .or("b.blocked is false")
                .append("a.id = c.account_id or b.blocked is false")
                .from("account a")
                .innerJoin("blocked b on ")
                .leftJoin("channel c")
                .select("a.*,b.*,c.*")
                .buildSql();
        assertThat(firstQuery).isEqualTo(secondQuery);
    }

}
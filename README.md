# simple-query-builder

This project aims to provide a simple Java SQL builder that makes it easier to create dynamic SQL queries without any need to use Java StringBuilder. It also supports a very basic form of DDD specification pattern.

You can use this builder to create dynamic JPQL queries since JPQL semantics are very similar to SQL. 

## Installation

### Maven

```xml
<dependency>
  <groupId>io.github.hbaykuslar</groupId>
  <artifactId>simple-query-builder</artifactId>
  <version>0.1.15</version>
</dependency>
```

### Gradle
```groovy
repositories {
  mavenCentral()
}
dependencies {
  implementation 'io.github.hbaykuslar:simple-query-builder:0.1.15'
}
```

## Usage

```java
//Create an sql query
String sql = new PlainSqlBuilder()
        .select("o.name", "count(1)")
        .from("orders o")
        .where("o.created_date > :startDate")
        .or("o.created_date <= :endDate")
        .groupBy("o.name")
        .having("count(1) > 2")
        .orderBy("o.name asc")
        .buildSql();
/*
            select o.name, count(1) 
            from orders o 
            where o.created_date > :startDate or o.created_date <= :endDate 
            group by o.name 
            having count(1) > 2 
            order by o.name asc
*/

// Create a specification and reuse it for multiple queries
var dateBetweenSpec = new Spec()
        .where("o.created_date > :startDate")
        .and("o.created_date <= :endDate");

String sql = new PlainSqlBuilder()
        .select("o.*")
        .from("orders o")
        .where(dateBetweenSpec)
        .and("o.id = :orderId")
        .orderBy("o.created_date desc", "o.name asc")
        .buildSql();
/*
            select o.* 
            from orders o 
            where (o.created_date > :startDate and o.created_date <= :endDate) 
                and o.id = :orderId 
            order by o.created_date desc, o.name asc
*/

// Create a query builder and use it as a subquery in where clouse
var topCustomersBuilder = new PlainSqlBuilder()
        .select("expensiveOrder.customer_id")
        .from("orders expensiveOrder")
        .orderBy("expensiveOrder.amount desc")
        .limit(3);

String sql = new PlainSqlBuilder()
        .select("o.*")
        .from("orders o")
        .where("o.id = :orderId")
        .andIn("o.customer_id", topCustomersBuilder)
        .buildSql();
/*
        select o.* 
        from orders o 
        where 
            o.id = :orderId and 
            o.customer_id in (select expensiveOrder.customer_id 
                            from orders expensiveOrder 
                            order by expensiveOrder.amount desc 
                            limit 3)
*/
```

For more usage examples you can check [tests](src/test/java/io/simplequerybuilder/PlainSqlBuilderTest.java)

## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

Please make sure to update tests as appropriate.


## License

[MIT](https://choosealicense.com/licenses/mit/)

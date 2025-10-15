package com.project.core_service.models.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Query} model.
 */
class QueryTest {

    @Test
    void builderSetsFieldsCorrectly() {
        Query query = Query.builder()
                .name("getUserByEmail")
                .query("SELECT * FROM users WHERE email = ?")
                .build();

        assertEquals("getUserByEmail", query.getName());
        assertEquals("SELECT * FROM users WHERE email = ?", query.getQuery());
    }

    @Test
    void shouldCreateQuerySuccessfully() {
        Query query = new Query(
                "findActiveOrders",
                "SELECT * FROM orders WHERE status = 'ACTIVE'");

        assertThat(query.getName()).isEqualTo("findActiveOrders");
        assertThat(query.getQuery()).isEqualTo("SELECT * FROM orders WHERE status = 'ACTIVE'");
    }

    @Test
    void shouldThrowExceptionWhenSettingNullForNonNullFields() {
        assertThatThrownBy(() -> new Query(
                null,
                "SELECT * FROM products"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Query(
                "getProducts",
                null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void builderShouldThrowExceptionForNullFields() {
        Query.QueryBuilder builder = Query.builder()
                .name("testQuery");

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void shouldRespectEqualsAndHashCode() {
        Query query1 = new Query(
                "sameQuery",
                "SELECT * FROM table1");

        Query query2 = new Query(
                "sameQuery",
                "SELECT * FROM table1");

        assertThat(query1).isEqualTo(query2);
        assertThat(query1.hashCode()).isEqualTo(query2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithDifferentData() {
        Query query1 = new Query(
                "query1",
                "SELECT * FROM table1");

        Query query2 = new Query(
                "query2",
                "SELECT * FROM table2");

        assertThat(query1).isNotEqualTo(query2);
    }

    @Test
    void toStringShouldContainMeaningfulInfo() {
        Query query = new Query(
                "getCustomers",
                "SELECT * FROM customers WHERE active = true");

        String output = query.toString();
        assertThat(output).contains("getCustomers");
        assertThat(output).contains("SELECT * FROM customers WHERE active = true");
    }

    @Test
    void noArgsConstructorShouldWork() {
        Query query = new Query();
        assertNotNull(query);
    }

    @Test
    void settersAndGettersShouldWork() {
        Query query = new Query();
        query.setName("updateUser");
        query.setQuery("UPDATE users SET last_login = NOW()");

        assertEquals("updateUser", query.getName());
        assertEquals("UPDATE users SET last_login = NOW()", query.getQuery());
    }
}

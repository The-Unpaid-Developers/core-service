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
                .mongoQuery("{\"email\": \"test@example.com\"}")
                .description("Retrieve user by email")
                .build();

        assertEquals("getUserByEmail", query.getName());
        assertEquals("{\"email\": \"test@example.com\"}", query.getMongoQuery());
        assertEquals("Retrieve user by email", query.getDescription());
    }

    @Test
    void shouldCreateQuerySuccessfully() {
        Query query = new Query(
                "findActiveOrders",
                "{\"status\": \"ACTIVE\"}",
                "Retrieve all active orders");

        assertThat(query.getName()).isEqualTo("findActiveOrders");
        assertThat(query.getMongoQuery()).isEqualTo("{\"status\": \"ACTIVE\"}");
        assertThat(query.getDescription()).isEqualTo("Retrieve all active orders");
    }

    @Test
    void shouldThrowExceptionWhenSettingNullForNonNullFields() {
        assertThatThrownBy(() -> new Query(
                null,
                "{\"category\": \"electronics\"}",
                "Retrieve all electronics products"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Query(
                "getProducts",
                null,
                "Retrieve all products"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Query(
                "getProducts",
                "{\"category\": \"electronics\"}",
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
                "{\"active\": true}", "test");

        Query query2 = new Query(
                "sameQuery",
                "{\"active\": true}", "test");

        assertThat(query1)
                .isEqualTo(query2)
                .hasSameHashCodeAs(query2);
    }

    @Test
    void shouldNotBeEqualWithDifferentData() {
        Query query1 = new Query(
                "query1",
                "{\"status\": \"active\"}", "test1");

        Query query2 = new Query(
                "query2",
                "{\"status\": \"inactive\"}", "test2");

        assertThat(query1).isNotEqualTo(query2);
    }

    @Test
    void toStringShouldContainMeaningfulInfo() {
        Query query = new Query(
                "getCustomers",
                "{\"active\": true}", "test");

        String output = query.toString();
        assertThat(output).contains("getCustomers", "{\"active\": true}", "test");
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
        query.setMongoQuery("{\"$set\": {\"lastLogin\": {\"$currentDate\": true}}}");
        query.setDescription("Update user's last login date");

        assertEquals("updateUser", query.getName());
        assertEquals("{\"$set\": {\"lastLogin\": {\"$currentDate\": true}}}", query.getMongoQuery());
        assertEquals("Update user's last login date", query.getDescription());
    }
}

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
                .build();

        assertEquals("getUserByEmail", query.getName());
        assertEquals("{\"email\": \"test@example.com\"}", query.getMongoQuery());
    }

    @Test
    void shouldCreateQuerySuccessfully() {
        Query query = new Query(
                "findActiveOrders",
                "{\"status\": \"ACTIVE\"}");

        assertThat(query.getName()).isEqualTo("findActiveOrders");
        assertThat(query.getMongoQuery()).isEqualTo("{\"status\": \"ACTIVE\"}");
    }

    @Test
    void shouldThrowExceptionWhenSettingNullForNonNullFields() {
        assertThatThrownBy(() -> new Query(
                null,
                "{\"category\": \"electronics\"}"))
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
                "{\"active\": true}");

        Query query2 = new Query(
                "sameQuery",
                "{\"active\": true}");

        assertThat(query1)
                .isEqualTo(query2)
                .hasSameHashCodeAs(query2);
    }

    @Test
    void shouldNotBeEqualWithDifferentData() {
        Query query1 = new Query(
                "query1",
                "{\"status\": \"active\"}");

        Query query2 = new Query(
                "query2",
                "{\"status\": \"inactive\"}");

        assertThat(query1).isNotEqualTo(query2);
    }

    @Test
    void toStringShouldContainMeaningfulInfo() {
        Query query = new Query(
                "getCustomers",
                "{\"active\": true}");

        String output = query.toString();
        assertThat(output).contains("getCustomers", "{\"active\": true}");
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

        assertEquals("updateUser", query.getName());
        assertEquals("{\"$set\": {\"lastLogin\": {\"$currentDate\": true}}}", query.getMongoQuery());
    }
}

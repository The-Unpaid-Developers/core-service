package com.project.core_service.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for RestTemplate with Apache HttpClient 5 connection pooling.
 *
 * <p>
 * This configuration provides an optimized RestTemplate that:
 * <ul>
 * <li>Uses connection pooling to reuse HTTP connections</li>
 * <li>Configures appropriate timeouts for external service calls</li>
 * <li>Manages connection lifecycle with idle connection eviction</li>
 * <li>Provides configurable pool sizes via application properties</li>
 * </ul>
 *
 * <p>
 * Connection pooling improves performance by avoiding the overhead of
 * establishing new TCP connections for each request, which is particularly
 * important for services that make frequent HTTP calls.
 */
@Configuration
public class RestTemplateConfig {

    @Value("${http.client.connection.timeout:30000}")
    private int connectionTimeout;

    @Value("${http.client.socket.timeout:30000}")
    private int socketTimeout;

    @Value("${http.client.pool.max-total:50}")
    private int maxTotal;

    @Value("${http.client.pool.max-per-route:20}")
    private int maxPerRoute;

    @Value("${http.client.pool.idle-timeout:30}")
    private int idleTimeout;

    @Value("${http.client.pool.validate-after-inactivity:5000}")
    private int validateAfterInactivity;

    /**
     * Creates a customized RestTemplateBuilder with connection pooling support.
     *
     * <p>
     * This builder uses Apache HttpClient 5 with a pooling connection manager,
     * providing the following benefits:
     * <ul>
     * <li>Reuses existing connections from the pool</li>
     * <li>Automatically evicts idle connections</li>
     * <li>Validates connections before reuse</li>
     * <li>Limits concurrent connections to prevent resource exhaustion</li>
     * </ul>
     *
     * @return a configured RestTemplateBuilder with connection pooling
     */
    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder()
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient()));
    }

    /**
     * Creates and configures the Apache HttpClient with connection pooling.
     *
     * <p>
     * Configuration includes:
     * <ul>
     * <li><b>Max Total Connections</b>: Maximum number of connections in the pool (default: 50)</li>
     * <li><b>Max Connections Per Route</b>: Maximum connections per target host (default: 20)</li>
     * <li><b>Connection Timeout</b>: Time to wait when establishing a connection (default: 30s)</li>
     * <li><b>Socket Timeout</b>: Time to wait for data after connection is established (default: 30s)</li>
     * <li><b>Idle Connection Eviction</b>: Removes connections idle for more than specified time</li>
     * <li><b>Connection Validation</b>: Validates connections that have been idle</li>
     * </ul>
     *
     * @return configured CloseableHttpClient instance
     */
    private CloseableHttpClient httpClient() {
        // Configure connection settings
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectionTimeout))
                .setSocketTimeout(Timeout.ofMilliseconds(socketTimeout))
                .setValidateAfterInactivity(TimeValue.ofMilliseconds(validateAfterInactivity))
                .build();

        // Create pooling connection manager
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .setMaxConnTotal(maxTotal)
                .setMaxConnPerRoute(maxPerRoute)
                .build();

        // Configure request settings
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectionTimeout))
                .build();

        // Build and return the HTTP client
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictIdleConnections(TimeValue.of(idleTimeout, TimeUnit.SECONDS))
                .evictExpiredConnections()
                .build();
    }
}

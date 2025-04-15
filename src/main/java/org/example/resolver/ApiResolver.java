package org.example.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.example.schema.DataSourceInfo;
import org.example.schema.DataSourceType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class ApiResolver implements Resolver {
    private final Executor executor = Executors.newFixedThreadPool(10);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .executor(executor)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, String> apiEndpoints = new HashMap<>();
    private Map<String, Object> config = new HashMap<>();
    
    @Override
    public boolean canResolve(DataSourceInfo dataSourceInfo) {
        return dataSourceInfo.getType() == DataSourceType.API;
    }

    @Override
    public DataFetcher<?> getDataFetcher(String typeName, String fieldName) {
        return environment -> resolveAsync(environment, typeName, environment.getArguments());
    }

    @Override
    public CompletableFuture<Map<String, Object>> resolveAsync(
            DataFetchingEnvironment environment, 
            String typeName, 
            Map<String, Object> arguments) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Resolving API data for type: {} with arguments: {}", typeName, arguments);
                
                // Get API endpoint
                String endpoint = apiEndpoints.get(typeName);
                if (endpoint == null) {
                    throw new IllegalStateException("No API endpoint configured for type: " + typeName);
                }
                
                // Build the URL with query parameters
                StringBuilder urlBuilder = new StringBuilder(endpoint);
                if (!arguments.isEmpty()) {
                    urlBuilder.append("?");
                    arguments.forEach((key, value) -> 
                        urlBuilder.append(key).append("=").append(value).append("&")
                    );
                    // Remove trailing &
                    urlBuilder.setLength(urlBuilder.length() - 1);
                }
                
                // Execute HTTP request
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlBuilder.toString()))
                        .header("Accept", "application/json")
                        .GET()
                        .build();
                
                HttpResponse<String> response = httpClient.send(
                        request, 
                        HttpResponse.BodyHandlers.ofString()
                );
                
                // Parse JSON response
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
                    result.put("resolvedBy", "ApiResolver");
                    log.info("Successfully resolved API data for type: {}", typeName);
                    return result;
                } else {
                    throw new RuntimeException("API request failed with status: " + response.statusCode());
                }
                
            } catch (Exception e) {
                log.error("Error resolving API data for type: {}", typeName, e);
                throw new RuntimeException("Failed to resolve API data for type: " + typeName, e);
            }
        }, executor);
    }

    @Override
    public void initialize(Map<String, Object> config) {
        this.config = config;
        
        // Extract API endpoint mappings from configuration
        if (config.containsKey("apiEndpoints") && config.get("apiEndpoints") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> endpoints = (Map<String, String>) config.get("apiEndpoints");
            this.apiEndpoints.putAll(endpoints);
        }
        
        log.info("Initialized ApiResolver with {} endpoint mappings", apiEndpoints.size());
    }
    
    /**
     * Register an API endpoint for a specific type
     */
    public void registerEndpoint(String typeName, String endpoint) {
        apiEndpoints.put(typeName, endpoint);
        log.info("Registered API endpoint for type {}: {}", typeName, endpoint);
    }
    
    /**
     * Configure authentication headers
     */
    public void configureAuth(String authType, Map<String, String> authParams) {
        this.config.put("authType", authType);
        this.config.put("authParams", authParams);
        log.info("Configured {} authentication for API resolver", authType);
    }
}

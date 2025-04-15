package org.example.resolver;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.example.schema.DataSourceInfo;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for all data resolvers that can fetch data from various sources
 */
public interface Resolver {
    
    /**
     * Determines if this resolver can handle the given data source
     */
    boolean canResolve(DataSourceInfo dataSourceInfo);
    
    /**
     * Returns a GraphQL DataFetcher for the specified type and field
     */
    DataFetcher<?> getDataFetcher(String typeName, String fieldName);
    
    /**
     * Asynchronously resolves data based on environment, type, and arguments
     */
    CompletableFuture<Map<String, Object>> resolveAsync(
            DataFetchingEnvironment environment, 
            String typeName,
            Map<String, Object> arguments);
    
    /**
     * Initializes the resolver with configuration
     */
    void initialize(Map<String, Object> config);
}

package org.example.resolver;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.example.schema.DataSourceInfo;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for GraphQL data resolvers
 */
public interface Resolver {
    /**
     * Check if this resolver can handle the given data source
     */
    boolean canResolve(DataSourceInfo dataSourceInfo);
    
    /**
     * Get a data fetcher for the specified type and field
     */
    DataFetcher<?> getDataFetcher(String typeName, String fieldName);
    
    /**
     * Resolve an object asynchronously
     */
    CompletableFuture<Map<String, Object>> resolveAsync(DataFetchingEnvironment environment, 
                                                        String typeName, 
                                                        Map<String, Object> arguments);
    
    /**
     * Initialize the resolver with configuration
     */
    void initialize(Map<String, Object> config);
}

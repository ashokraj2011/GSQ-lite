package org.example.resolver;

import lombok.extern.slf4j.Slf4j;
import org.example.schema.DataSourceInfo;
import org.example.schema.DataSourceType;
import org.example.schema.GraphQLSchemaReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory class responsible for creating and managing resolvers for different data sources
 */
@Slf4j
public class ResolverFactory {
    
    private final Map<String, Resolver> typeResolverMap = new HashMap<>();
    private final ApiResolver apiResolver = new ApiResolver();
    private final DatabaseResolver databaseResolver = new DatabaseResolver();
    
    /**
     * Configure resolvers with appropriate settings
     */
    public void configureResolvers(GraphQLSchemaReader schemaReader) {
        log.info("Configuring resolvers based on schema information");
        
        // Configure API resolver
        Map<String, Object> apiConfig = new HashMap<>();
        Map<String, String> apiEndpoints = new HashMap<>();
        
        List<DataSourceInfo> apiSources = schemaReader.getApiDataSources();
        for (DataSourceInfo api : apiSources) {
            apiEndpoints.put(api.getTypeName(), api.getSource());
        }
        
        apiConfig.put("apiEndpoints", apiEndpoints);
        apiResolver.initialize(apiConfig);
        
        // Configure Database resolver
        Map<String, Object> dbConfig = new HashMap<>();
        Map<String, String> entityMappings = new HashMap<>();
        
        List<DataSourceInfo> dbSources = schemaReader.getDatabaseDataSources();
        for (DataSourceInfo db : dbSources) {
            entityMappings.put(db.getTypeName(), db.getSource());
        }
        
        dbConfig.put("entityMappings", entityMappings);
        databaseResolver.initialize(dbConfig);
        
        log.info("Resolvers configured successfully");
    }
    
    /**
     * Initialize type-to-resolver mapping from schema data sources
     */
    public void initializeFromSchema(GraphQLSchemaReader schemaReader) {
        log.info("Initializing type-to-resolver mappings");
        
        for (DataSourceInfo dataSource : schemaReader.getDataSourceMapping().values()) {
            Resolver resolver = null;
            
            if (dataSource.getType() == DataSourceType.API) {
                resolver = apiResolver;
            } else if (dataSource.getType() == DataSourceType.DATABASE) {
                resolver = databaseResolver;
            } else if (dataSource.getType() == DataSourceType.FILE) {
                // TODO: Implement file resolver
                log.warn("File resolver not yet implemented for type: {}", dataSource.getTypeName());
                continue;
            }
            
            if (resolver != null) {
                typeResolverMap.put(dataSource.getTypeName(), resolver);
                log.info("Mapped type '{}' to resolver: {}", dataSource.getTypeName(), 
                         resolver.getClass().getSimpleName());
            }
        }
        
        log.info("Initialized {} type-resolver mappings", typeResolverMap.size());
    }
    
    /**
     * Get the resolver for a specific type
     */
    public Resolver getResolverForType(String typeName) {
        return typeResolverMap.get(typeName);
    }
    
    /**
     * Get all configured resolvers
     */
    public Map<String, Resolver> getAllResolvers() {
        return new HashMap<>(typeResolverMap);
    }
}

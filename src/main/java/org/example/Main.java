package org.example;

import org.example.resolver.ApiResolver;
import org.example.resolver.DatabaseResolver;
import org.example.resolver.ResolverFactory;
import org.example.schema.DataSourceInfo;
import org.example.schema.GraphQLSchemaReader;
import org.example.schema.SchemaAnalyzer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Path to the GraphQL schema file
            Path schemaPath = Paths.get("schema.graphql");
            
            // Parse the schema
            GraphQLSchemaReader schemaReader = GraphQLSchemaReader.fromFile(schemaPath);
            
            // Display basic information
            System.out.println("Successfully loaded GraphQL schema");
            System.out.println("Total types: " + schemaReader.getTypeDefinitions().size());
            
            // Show data sources
            List<DataSourceInfo> apiSources = schemaReader.getApiDataSources();
            System.out.println("\nAPI Data Sources:");
            apiSources.forEach(source -> 
                System.out.println("- " + source.getTypeName() + ": " + source.getSource()));
            
            List<DataSourceInfo> fileSources = schemaReader.getFileDataSources();
            System.out.println("\nFile Data Sources:");
            fileSources.forEach(source -> 
                System.out.println("- " + source.getTypeName() + ": " + source.getSource()));
            
            List<DataSourceInfo> dbSources = schemaReader.getDatabaseDataSources();
            System.out.println("\nDatabase Data Sources:");
            dbSources.forEach(source -> 
                System.out.println("- " + source.getTypeName() + ": " + source.getSource()));
            
            // Analyze the schema
            SchemaAnalyzer analyzer = new SchemaAnalyzer(schemaReader);
            System.out.println("\nSchema Statistics:");
            analyzer.printSchemaStatistics();
            
            // Initialize resolvers
            System.out.println("\nInitializing resolvers...");
            ResolverFactory resolverFactory = new ResolverFactory();
            resolverFactory.configureResolvers(schemaReader);
            resolverFactory.initializeFromSchema(schemaReader);
            
            // Test resolvers
            System.out.println("\nTesting resolvers...");
            if (!dbSources.isEmpty()) {
                String dbTypeName = dbSources.get(0).getTypeName();
                System.out.println("Database resolver for type " + dbTypeName + ": " + 
                                  (resolverFactory.getResolverForType(dbTypeName) instanceof DatabaseResolver));
            }
            
            if (!apiSources.isEmpty()) {
                String apiTypeName = apiSources.get(0).getTypeName();
                System.out.println("API resolver for type " + apiTypeName + ": " + 
                                  (resolverFactory.getResolverForType(apiTypeName) instanceof ApiResolver));
            }
            
        } catch (Exception e) {
            System.err.println("Error processing GraphQL schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

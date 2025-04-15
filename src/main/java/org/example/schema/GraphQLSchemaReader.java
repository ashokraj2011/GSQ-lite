package org.example.schema;

import graphql.language.*;
import graphql.parser.Parser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class GraphQLSchemaReader {
    
    @Getter
    private Document schemaDocument;
    
    @Getter
    private final Map<String, TypeDefinition<?>> typeDefinitions = new HashMap<>();
    
    @Getter
    private final Map<String, ObjectTypeDefinition> queryTypes = new HashMap<>();
    
    @Getter
    private final Map<String, DataSourceInfo> dataSourceMapping = new HashMap<>();
    
    public GraphQLSchemaReader(String schemaContent) {
        parse(schemaContent);
        extractTypeDefinitions();
        extractDataSources();
    }
    
    public static GraphQLSchemaReader fromFile(Path schemaPath) throws IOException {
        String schemaContent = Files.readString(schemaPath);
        return new GraphQLSchemaReader(schemaContent);
    }
    
    private void parse(String schemaContent) {
        Parser parser = new Parser();
        schemaDocument = parser.parseDocument(schemaContent);
        log.info("Successfully parsed GraphQL schema");
    }
    
    private void extractTypeDefinitions() {
        schemaDocument.getDefinitions().forEach(definition -> {
            if (definition instanceof TypeDefinition) {
                TypeDefinition<?> typeDefinition = (TypeDefinition<?>) definition;
                typeDefinitions.put(typeDefinition.getName(), typeDefinition);
                
                // Identify Query type which serves as root entry point
                if (typeDefinition.getName().equals("Query") && typeDefinition instanceof ObjectTypeDefinition) {
                    extractQueryFields((ObjectTypeDefinition) typeDefinition);
                }
            }
        });
        log.info("Extracted {} type definitions from schema", typeDefinitions.size());
    }
    
    private void extractQueryFields(ObjectTypeDefinition queryType) {
        queryType.getFieldDefinitions().forEach(field -> {
            queryTypes.put(field.getName(), queryType);
        });
        log.info("Extracted {} query fields", queryTypes.size());
    }
    
    private void extractDataSources() {
        typeDefinitions.values().stream()
                .filter(type -> type instanceof ObjectTypeDefinition)
                .map(type -> (ObjectTypeDefinition) type)
                .forEach(this::processTypeForDataSource);
        
        log.info("Extracted {} data source mappings", dataSourceMapping.size());
    }
    
    private void processTypeForDataSource(ObjectTypeDefinition type) {
        String typeName = type.getName();
        
        // Check for API data source
        getDirectiveArgument(type, "api", "url").ifPresent(url -> {
            dataSourceMapping.put(typeName, new DataSourceInfo(typeName, DataSourceType.API, url));
        });
        
        // Check for File data source
        getDirectiveArgument(type, "source", "file").ifPresent(file -> {
            dataSourceMapping.put(typeName, new DataSourceInfo(typeName, DataSourceType.FILE, file));
        });
        
        // Check for Database data source
        getDirectiveArgument(type, "db", "entity").ifPresent(entity -> {
            dataSourceMapping.put(typeName, new DataSourceInfo(typeName, DataSourceType.DATABASE, entity));
        });
    }
    
    private Optional<String> getDirectiveArgument(ObjectTypeDefinition type, String directiveName, String argumentName) {
        return type.getDirectives().stream()
                .filter(directive -> directive.getName().equals(directiveName))
                .flatMap(directive -> directive.getArguments().stream())
                .filter(argument -> argument.getName().equals(argumentName))
                .map(argument -> {
                    Value value = argument.getValue();
                    if (value instanceof StringValue) {
                        return ((StringValue) value).getValue();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst();
    }
    
    public List<DataSourceInfo> getApiDataSources() {
        return dataSourceMapping.values().stream()
                .filter(info -> info.getType() == DataSourceType.API)
                .collect(Collectors.toList());
    }
    
    public List<DataSourceInfo> getFileDataSources() {
        return dataSourceMapping.values().stream()
                .filter(info -> info.getType() == DataSourceType.FILE)
                .collect(Collectors.toList());
    }
    
    public List<DataSourceInfo> getDatabaseDataSources() {
        return dataSourceMapping.values().stream()
                .filter(info -> info.getType() == DataSourceType.DATABASE)
                .collect(Collectors.toList());
    }
    
    public Map<String, List<FieldDefinition>> getObjectFields() {
        Map<String, List<FieldDefinition>> result = new HashMap<>();
        
        typeDefinitions.values().stream()
                .filter(type -> type instanceof ObjectTypeDefinition)
                .forEach(type -> {
                    ObjectTypeDefinition objectType = (ObjectTypeDefinition) type;
                    result.put(objectType.getName(), objectType.getFieldDefinitions());
                });
        
        return result;
    }
}

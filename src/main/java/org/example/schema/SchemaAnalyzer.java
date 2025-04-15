package org.example.schema;

import graphql.language.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SchemaAnalyzer {
    
    private final GraphQLSchemaReader schemaReader;
    
    public SchemaAnalyzer(GraphQLSchemaReader schemaReader) {
        this.schemaReader = schemaReader;
    }
    
    public Map<String, Set<String>> analyzeTypeRelationships() {
        Map<String, Set<String>> relationships = new HashMap<>();
        
        schemaReader.getTypeDefinitions().values().stream()
                .filter(type -> type instanceof ObjectTypeDefinition)
                .forEach(type -> {
                    String typeName = type.getName();
                    relationships.put(typeName, new HashSet<>());
                    
                    ((ObjectTypeDefinition) type).getFieldDefinitions().forEach(field -> {
                        Type<?> fieldType = field.getType();
                        String referencedType = getBaseTypeName(fieldType);
                        
                        if (schemaReader.getTypeDefinitions().containsKey(referencedType) && 
                            !referencedType.equals(typeName)) {
                            relationships.get(typeName).add(referencedType);
                        }
                    });
                });
        
        return relationships;
    }
    
    private String getBaseTypeName(Type<?> type) {
        if (type instanceof ListType) {
            return getBaseTypeName(((ListType) type).getType());
        } else if (type instanceof NonNullType) {
            return getBaseTypeName(((NonNullType) type).getType());
        } else if (type instanceof TypeName) {
            return ((TypeName) type).getName();
        }
        return "";
    }
    
    public List<String> findOrphanedTypes() {
        Map<String, Set<String>> relationships = analyzeTypeRelationships();
        Set<String> referencedTypes = relationships.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        
        return relationships.keySet().stream()
                .filter(type -> !referencedTypes.contains(type) && !type.equals("Query"))
                .collect(Collectors.toList());
    }
    
    public Map<String, Integer> countFieldsPerType() {
        Map<String, Integer> fieldCounts = new HashMap<>();
        
        schemaReader.getTypeDefinitions().values().stream()
                .filter(type -> type instanceof ObjectTypeDefinition)
                .forEach(type -> {
                    String typeName = type.getName();
                    int fieldCount = ((ObjectTypeDefinition) type).getFieldDefinitions().size();
                    fieldCounts.put(typeName, fieldCount);
                });
        
        return fieldCounts;
    }
    
    public void printSchemaStatistics() {
        log.info("=== GraphQL Schema Statistics ===");
        log.info("Total types: {}", schemaReader.getTypeDefinitions().size());
        log.info("Query fields: {}", schemaReader.getQueryTypes().size());
        log.info("API data sources: {}", schemaReader.getApiDataSources().size());
        log.info("File data sources: {}", schemaReader.getFileDataSources().size());
        log.info("Database data sources: {}", schemaReader.getDatabaseDataSources().size());
        
        Map<String, Integer> fieldCounts = countFieldsPerType();
        OptionalDouble avgFields = fieldCounts.values().stream().mapToInt(Integer::intValue).average();
        log.info("Average fields per type: {}", avgFields.orElse(0));
        
        List<String> orphanedTypes = findOrphanedTypes();
        log.info("Orphaned types (not referenced by other types): {}", orphanedTypes);
    }
}

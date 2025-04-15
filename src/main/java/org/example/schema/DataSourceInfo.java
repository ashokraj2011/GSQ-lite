package org.example.schema;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DataSourceInfo {
    private String typeName;
    private DataSourceType type;
    private String source; // URL, file path, or entity name depending on type
}

package com.rikkeiexpress.ai.util;

import java.util.regex.Pattern;

public class SafeSqlValidator {
    private static final Pattern READ_ONLY_PATTERN = Pattern.compile(
        "^(?i)\\s*SELECT\\s+.*", Pattern.DOTALL
    );
    
    private static final Pattern DESTRUCTIVE_PATTERN = Pattern.compile(
        "(?i)\\b(INSERT|UPDATE|DELETE|DROP|ALTER|TRUNCATE|RENAME|CREATE|GRANT|REVOKE|REPLACE|EXECUTE|EXEC|MERGE)\\b"
    );

    public static void validate(String sqlQuery) {
        if (sqlQuery == null || sqlQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query cannot be empty");
        }
        
        String cleanSql = sqlQuery.trim();
        
        if (!READ_ONLY_PATTERN.matcher(cleanSql).matches()) {
            throw new SecurityException("Security Violation: Only SELECT statements are allowed.");
        }
        
        if (DESTRUCTIVE_PATTERN.matcher(cleanSql).find()) {
            throw new SecurityException("Security Violation: Destructive operations detected in your SELECT query.");
        }
    }
}
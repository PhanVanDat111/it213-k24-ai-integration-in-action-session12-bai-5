package com.rikkeiexpress.ai.config;

import com.rikkeiexpress.ai.service.DatabaseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.tool.annotation.Tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class McpServerConfig {

    private final DatabaseService databaseService;
    private static final String SAFE_DIRECTORY = "C:/data/reports/";

    public McpServerConfig(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Bean
    public String logisticsSchemaResource() {
        return databaseService.getSchema();
    }

    @Tool(description = "Executes a clean, read-only SELECT query against the H2 logistics database schema.")
    public String execute_read_only_query(String sqlQuery) {
        return databaseService.executeQuery(sqlQuery);
    }

    @Tool(description = "Saves a markdown-formatted report safely into C:/data/reports/ after strict Path Traversal check.")
    public String export_markdown_report(String fileName, String markdownContent) {
        try {
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                throw new SecurityException("Potential Path Traversal Attack detected in file name!");
            }
            
            File dir = new File(SAFE_DIRECTORY);
            if (!dir.exists()) {
                Files.createDirectories(Paths.get(SAFE_DIRECTORY));
            }

            File targetFile = new File(dir, fileName);
            try (FileWriter writer = new FileWriter(targetFile)) {
                writer.write(markdownContent);
            }
            
            return "Successfully exported report to: " + targetFile.getAbsolutePath();
        } catch (IOException | SecurityException e) {
            return "Failed to export report: " + e.getMessage();
        }
    }
}
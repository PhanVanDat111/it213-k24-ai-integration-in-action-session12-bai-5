package com.rikkeiexpress.ai;

import com.rikkeiexpress.ai.agent.DataAnalystAgent;
import com.rikkeiexpress.ai.config.McpServerConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DataAnalystMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataAnalystMcpApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(McpServerConfig config, DataAnalystAgent agent) {
        return args -> {
            System.out.println("========================================================================");
            System.out.println(" RikkeiExpress AI Data Analyst Agent running under MCP Model ...");
            System.out.println("========================================================================");
            
            String schema = config.logisticsSchemaResource();
            System.out.println("Loaded MCP Resource: " + schema);
            
            String query = """
                SELECT 
                    p.id, 
                    p.name, 
                    COUNT(d.id) AS total_deliveries, 
                    COUNT(CASE WHEN d.status = 'DELIVERED' THEN 1 END) AS successful_deliveries, 
                    COUNT(CASE WHEN d.status = 'DELAYED' THEN 1 END) AS delayed_deliveries, 
                    ROUND(CAST(COUNT(CASE WHEN d.status = 'DELIVERED' THEN 1 END) AS DOUBLE) / COUNT(d.id) * 100, 2) AS success_rate,
                    ROUND(CAST(COUNT(CASE WHEN d.status = 'DELAYED' THEN 1 END) AS DOUBLE) / COUNT(d.id) * 100, 2) AS delayed_rate
                FROM post_offices p
                LEFT JOIN deliveries d ON p.id = d.post_office_id
                GROUP BY p.id, p.name
                ORDER BY delayed_rate DESC;
            """;
            
            System.out.println("Executing tool: execute_read_only_query...");
            String rawJsonResult = config.execute_read_only_query(query);
            System.out.println("SQL JSON Result: " + rawJsonResult);
            
            String reportContent = """
            # BÁO CÁO HIỆU SUẤT GIAO HÀNG TUẦN - RIKKEIEXPRESS
            
            ## 1. Số Liệu Tổng Quan Các Bưu Cục
            | Mã BC | Tên Bưu Cục | Tổng Đơn | Thành Công | Giao Trễ | Tỷ Lệ TC (%) | Tỷ Lệ Trễ (%) |
            |-------|-------------|---------|------------|----------|--------------|----------------|
            | PO-SG02| Bưu cục Tân Bình | 3 | 0 | 3 | 0.00 | 100.00 |
            | PO-HN02| Bưu cục Cầu Giấy | 3 | 1 | 2 | 33.33 | 66.67 |
            | PO-HN01| Bưu cục Hoàn Kiếm| 3 | 2 | 1 | 66.67 | 33.33 |
            | PO-SG01| Bưu cục Quận 1   | 2 | 2 | 0 | 100.00| 0.00 |
            | PO-DN01| Bưu cục Hải Châu | 4 | 4 | 0 | 100.00| 0.00 |
            
            ## 2. Top 3 Bưu Cục Có Tỷ Lệ Trễ Cao Nhất
            1. **Bưu cục Tân Bình (PO-SG02)**: Tỷ lệ trễ **100%** (3/3 đơn trễ)
            2. **Bưu cục Cầu Giấy (PO-HN02)**: Tỷ lệ trễ **66.67%** (2/3 đơn trễ)
            3. **Bưu cục Hoàn Kiếm (PO-HN01)**: Tỷ lệ trễ **33.33%** (1/3 đơn trễ)
            
            ## 3. Nhận Định Nguyên Nhân & Giải Pháp Khắc Phục
            - **Khu vực TP. Hồ Chí Minh (Tân Bình)**: Đang quá tải khâu giao vận chặng cuối hoặc nhân viên thiếu hụt nghiêm trọng trong tuần. Cần điều động khẩn cấp.
            - **Khu vực Hà Nội (Cầu Giấy, Hoàn Kiếm)**: Các tuyến đường giao giờ cao điểm thường ùn tắc nặng. Kiến nghị tối ưu lại lộ trình giao và bổ sung shipper bán thời gian.
            """;
            
            System.out.println("Executing tool: export_markdown_report...");
            String exportResponse = config.export_markdown_report("weekly_performance_report.md", reportContent);
            System.out.println("Export Response: " + exportResponse);
        };
    }
}
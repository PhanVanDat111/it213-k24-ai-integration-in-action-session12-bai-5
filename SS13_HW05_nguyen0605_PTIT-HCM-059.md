# BÁO CÁO THIẾT KẾ: AI DATA ANALYST AGENT TỰ ĐỘNG HÓA QUA MCP

## 1. Tóm tắt yêu cầu nghiệp vụ
- **Bối cảnh**: Ban Giám đốc RikkeiExpress yêu cầu báo cáo tự động mỗi sáng thứ Hai: Thống kê tỷ lệ đơn hàng thành công, giao trễ của từng bưu cục tuần qua. Chỉ ra Top 3 bưu cục trễ cao nhất và đề xuất nguyên nhân, sau đó ghi kết quả vào tệp `weekly_performance_report.md` tại `C:/data/reports/`.
- **Mục tiêu**: Thiết kế hệ thống AI Agent sử dụng Model Context Protocol (MCP) và Spring AI để hoàn thành toàn bộ công việc phân tích, thống kê dữ liệu, tổng hợp nội dung và xuất file trong dưới 1 phút.

---

## 2. Sơ đồ Luồng Tương tác (Agent Loop - ASCII Diagram)

```
+-----------------------+              (1) Request: "Báo cáo hiệu suất"             +-----------------------+
|  Ban Giám Đốc (User)  | ========================================================> |  AI Data Analyst Agent|
+-----------------------+                                                           |     (Spring AI)       |
                                                                                    +-----------------------+
                                                                                                ||
+-----------------------+                Phản hồi thông tin schema                      || (2) Đọc resource
|      MCP Server       | <=============================================================||
|  (logistics-schema)   |                                                               ||
+-----------------------+                                                               ||
                                                                                        ||
+-----------------------+            (3) execute_read_only_query (sqlQuery)             ||
|      MCP Tool         | <=============================================================||
| (SQL Query Execution) | =============================================================>||
+-----------------------+               Trả về kết quả thống kê dạng JSON                ||
                                                                                        ||
                                                                                        || (4) Phân tích
                                                                                        ||     & Tổng hợp
                                                                                        ||
+-----------------------+             (5) export_markdown_report (file, content)        ||
|      MCP Tool         | <=============================================================||
| (Markdown Export)     | =============================================================>||
+-----------------------+                        Thông báo ghi file thành công
```

---

## 3. Giả lập Phiên Hội Thoại Thực Tế giữa AI và Hệ Thống

### **Lượt 1: Quản lý khởi tạo yêu cầu**
- **User**: "Hãy tạo báo cáo hiệu suất giao hàng tuần qua của tất cả bưu cục, phân tích top 3 nơi trễ nhiều nhất và xuất file `weekly_performance_report.md`."
- **Agent (Context Discovery)**:
  *   *Hành động*: Gọi tài nguyên `resource://db/logistics-schema`.
  *   *Dữ liệu trả về*: Cấu trúc hai bảng `post_offices` và `deliveries`.

### **Lượt 2: Thiết lập kế hoạch và Truy vấn SQL**
- **Agent (Planning & Execution)**:
  *   *Hành động*: Tạo câu lệnh SQL tối ưu tính toán tỷ lệ thành công và trễ của từng bưu cục.
  *   *Công cụ sử dụng*: `execute_read_only_query` với câu lệnh SQL:
    ```sql
    SELECT 
        p.id, p.name, 
        COUNT(d.id) AS total_deliveries,
        COUNT(CASE WHEN d.status = 'DELIVERED' THEN 1 END) AS success_count,
        COUNT(CASE WHEN d.status = 'DELAYED' THEN 1 END) AS delayed_count,
        ROUND(CAST(COUNT(CASE WHEN d.status = 'DELIVERED' THEN 1 END) AS DOUBLE) / COUNT(d.id) * 100, 2) AS success_rate,
        ROUND(CAST(COUNT(CASE WHEN d.status = 'DELAYED' THEN 1 END) AS DOUBLE) / COUNT(d.id) * 100, 2) AS delayed_rate
    FROM post_offices p
    LEFT JOIN deliveries d ON p.id = d.post_office_id
    GROUP BY p.id, p.name
    ORDER BY delayed_rate DESC;
    ```
  *   *Dữ liệu thô trả về từ Tool (JSON)*:
    ```json
    [
      {"ID":"PO-SG02","NAME":"Bưu cục Tân Bình","TOTAL_DELIVERIES":3,"SUCCESS_COUNT":0,"DELAYED_COUNT":3,"SUCCESS_RATE":0.0,"DELAYED_RATE":100.0},
      {"ID":"PO-HN02","NAME":"Bưu cục Cầu Giấy","TOTAL_DELIVERIES":3,"SUCCESS_COUNT":1,"DELAYED_COUNT":2,"SUCCESS_RATE":33.33,"DELAYED_RATE":66.67},
      {"ID":"PO-HN01","NAME":"Bưu cục Hoàn Kiếm","TOTAL_DELIVERIES":3,"SUCCESS_COUNT":2,"DELAYED_COUNT":1,"SUCCESS_RATE":66.67,"DELAYED_RATE":33.33},
      {"ID":"PO-SG01","NAME":"Bưu cục Quận 1","TOTAL_DELIVERIES":2,"SUCCESS_COUNT":2,"DELAYED_COUNT":0,"SUCCESS_RATE":100.0,"DELAYED_RATE":0.0},
      {"ID":"PO-DN01","NAME":"Bưu cục Hải Châu","TOTAL_DELIVERIES":4,"SUCCESS_COUNT":4,"DELAYED_COUNT":0,"SUCCESS_RATE":100.0,"DELAYED_RATE":0.0}
    ]
    ```

### **Lượt 3: Tổng hợp phân tích & Xuất bản Artifact**
- **Agent (Synthesis & Generation)**:
  *   *Hành động*: Tổng hợp dữ liệu thành báo cáo Markdown hoàn chỉnh, đánh giá top 3 bưu cục trễ nhiều nhất (Tân Bình, Cầu Giấy, Hoàn Kiếm), suy luận nguyên nhân quá tải và ùn tắc cục bộ.
  *   *Công cụ sử dụng*: Gọi `export_markdown_report` với tham số `fileName="weekly_performance_report.md"` cùng nội dung báo cáo chi tiết.
  *   *Phản hồi của Tool*: `"Successfully exported report to: C:\data\reports\weekly_performance_report.md"`.
  *   *Phản hồi cuối cùng với User*: "Báo cáo hiệu suất bưu cục tuần qua đã được tổng hợp thành công và lưu trữ an toàn tại `C:/data/reports/weekly_performance_report.md`."
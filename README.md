# Intern Management

Hệ thống quản lý thực tập xây dựng bằng Java Servlet/JSP, MySQL và Maven.

## Yêu cầu

- JDK 21
- Maven 3.9+
- MySQL 8
- Tomcat 10.1+

## Cấu hình database

Khuyến nghị khai báo ba biến môi trường:

```text
IMS_DB_URL=jdbc:mysql://localhost:3306/internship_management?useUnicode=true&characterEncoding=UTF-8
IMS_DB_USER=root
IMS_DB_PASSWORD=<mat-khau-mysql>
```

Khi chạy local, có thể sao chép `src/main/resources/db-config.example.json` thành
`src/main/resources/db-config.json` và thay thông tin tương ứng. File `db-config.json`
đã bị Git bỏ qua vì có chứa thông tin bí mật.

## Khởi tạo dữ liệu

1. Tạo/kết nối MySQL bằng tài khoản đã cấu hình.
2. Chạy lần lượt:
   - `src/main/resources/schema.sql`
   - `src/main/resources/migration-001-rbac.sql`
   - `src/main/resources/demo-data.sql`

## Build và chạy

```bash
mvn clean test
mvn package
```

Sau khi build, deploy `target/Internmanagement.war` lên Tomcat 10.1+, sau đó truy cập:

```text
http://localhost:8080/Internmanagement/login
```

Không commit `target/`, `tmp/`, `node_modules/`, file cookie hoặc file cấu hình
chứa mật khẩu.

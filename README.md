# 🛒 E-Commerce Website - Shop Localbrand

Shop Localbrand la 1 website don gian, cho phep nguoi dung co the dang ky/ dang nhap vao, xem san pham, mua va dat hang, co thanh toan bang VNPay
Du an duoc phat trien tren Springboot + Thymleaf + Bootstrap 

--------

## Demo
Link demo: https://lenup.asia/home
Trang chu: <img width="1916" height="951" alt="Screenshot 2025-09-02 154432" src="https://github.com/user-attachments/assets/1421defc-cd7f-4fe8-85bd-48c87ed8cb82" />

Trang san pham:<img width="1916" height="947" alt="Screenshot 2025-09-02 154523" src="https://github.com/user-attachments/assets/0c681dee-062f-4824-ae4b-ad89ec393638" />

Trang gio hang:<img width="1917" height="953" alt="Screenshot 2025-09-02 154611" src="https://github.com/user-attachments/assets/83c1271f-6dd3-4583-8289-669841e8fa5b" />

Trang chi tiet san pham:<img width="1917" height="953" alt="Screenshot 2025-09-02 154647" src="https://github.com/user-attachments/assets/87a1d33c-bc0c-48da-85b6-3db908936098" />

Trang thanh toan bang VNPay:<img width="1912" height="952" alt="Screenshot 2025-09-02 154842" src="https://github.com/user-attachments/assets/422ccbf8-f7de-45c8-a9f3-e92f025a6108" />

--------
## ✨ Tính năng
- 👤 Đăng ký / Đăng nhập (tài khoản, Google, Facebook)
- 🔍 Xem & tìm kiếm sản phẩm
- 🛒 Giỏ hàng & thanh toán VNPay
- 📦 Theo dõi đơn hàng
- 📊 Admin / Staff Dashboard
- 🎨 Lọc sản phẩm theo danh mục, giá, size, màu sắc
- 📝 Thông tin cá nhân
- ......

--------

## Cấu trúc thư mục
```
src/
 ├── main/
 │   ├── java/com/poly/asm
 │   │    ├── controller/   # Controller xử lý request (HomeController, ProductController, ...)
 │   │    ├── daos/         # DAO layer (JPA Repository)
 │   │    ├── entitys/      # Các Entity (Account, Product, Order, ...)
 │   │    └── services/     # Business logic (CartService, UserService, ...)
 │   │
 │   ├── resources/
 │   │    ├── static/       # Tài nguyên tĩnh
 │   │    │     ├── css/    # File CSS
 │   │    │     ├── js/     # File JavaScript
 │   │    │     └── images/ # Hình ảnh
 │   │    │
 │   │    ├── templates/    # Giao diện Thymeleaf
 │   │    │     ├── admin/      # Trang quản trị
 │   │    │     ├── cart/       # Trang giỏ hàng
 │   │    │     ├── fragments/  # Header, Footer, layout chung
 │   │    │     ├── staff/      # Trang quản lý cho staff
 │   │    │     └── web/        # Trang web chính (home, sản phẩm, ...)
 │   │
 │   │    └── application.properties # Cấu hình ứng dụng
 │
 └── pom.xml   # Cấu hình Maven
```

--------

## Cai dat

### Yeu cau
- Java 17+
- Maven 3.8+
- SQL Server 18+
- Springboot

## Cau hinh cai dat application.properties
- spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=LENUP;encrypt=false;trustServerCertificate=true
- spring.datasource.username=sa
- spring.datasource.password=123
- spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# Chay ung dung ( cach 1 )
Ctrl + Alt + Shift + B, sau do nhan R ( thao tac phim tat nhanh tren springtool )
# Chạy ứng dụng (cach 2 )
mvn spring-boot:run

--------

## 📄 License
Dự án được phát hành theo giấy phép [MIT](LICENSE).

--------

## 🤝 Đóng góp
Mọi đóng góp đều được hoan nghênh!  

Cách thức đóng góp:
1. Fork dự án này
2. Tạo branch mới (`feature/ten-chuc-nang`)
3. Commit thay đổi của bạn
4. Push lên branch vừa tạo
5. Gửi Pull Request 🚀

--------

## 👨‍💻 Tác giả ( Nhom )
- Hoàng Chương Võ - [GitHub](https://github.com/chuonghoang123) | [LinkedIn](linkedin.com/in/hoàng-chương-võ-aa646835a)
- 

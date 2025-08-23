-- Tạo database
USE master;
DROP DATABASE IF EXISTS LENUP;
CREATE DATABASE LENUP COLLATE Vietnamese_CI_AS;
USE LENUP;

-- Bảng Users
CREATE TABLE Users (
    id INT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(50) UNIQUE NOT NULL,
    password NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    fullname NVARCHAR(100),
    phone NVARCHAR(10),
    address NVARCHAR(255),
    role NVARCHAR(20) CHECK (role IN ('USER', 'ADMIN', 'STAFF')) DEFAULT 'USER',
    active BIT DEFAULT 1,
    provider NVARCHAR(50),
    providerId NVARCHAR(255)
);

-- Bảng Categories
CREATE TABLE Categories (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) UNIQUE NOT NULL
);

-- Bảng Products
CREATE TABLE Products (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(500),
    category_id INT NOT NULL FOREIGN KEY REFERENCES Categories(id)
);

-- Bảng ProductVariants (thể hiện size, color, giá, stock)
CREATE TABLE ProductVariants (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    product_id BIGINT FOREIGN KEY REFERENCES Products(id),
    size NVARCHAR(10) NOT NULL,
    color NVARCHAR(50) NOT NULL,
    stock INT NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    UNIQUE(product_id, size, color)
);

-- Bảng ProductImages
CREATE TABLE ProductImages (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    product_id BIGINT FOREIGN KEY REFERENCES Products(id),
    variant_id BIGINT FOREIGN KEY REFERENCES ProductVariants(id),
    image_url NVARCHAR(255) NOT NULL
);

-- Bảng Orders
CREATE TABLE Orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,      
    fullname NVARCHAR(255) NOT NULL,         
    phone NVARCHAR(10) NOT NULL,                
    address NVARCHAR(255) NOT NULL,            
    payment_method NVARCHAR(50) NOT NULL,      
    total_price FLOAT NOT NULL,                 
    order_date DATETIME DEFAULT GETDATE(),
    status VARCHAR(255),
    user_id INT FOREIGN KEY REFERENCES Users(id)
);

-- Bảng OrderDetails
CREATE TABLE OrderDetails (
    id INT PRIMARY KEY IDENTITY(1,1),
    order_id BIGINT FOREIGN KEY REFERENCES Orders(id),
    variant_id BIGINT FOREIGN KEY REFERENCES ProductVariants(id),
    quantity INT NOT NULL,
    price DECIMAL(18,2) NOT NULL
);

-- Bảng Carts
CREATE TABLE Carts (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT FOREIGN KEY REFERENCES Users(id),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Bảng CartItems
CREATE TABLE CartItems (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    cart_id INT FOREIGN KEY REFERENCES Carts(id),
    variant_id BIGINT FOREIGN KEY REFERENCES ProductVariants(id),
    quantity INT NOT NULL,
    price FLOAT NOT NULL,
    productName NVARCHAR(255) NOT NULL DEFAULT '',
    color NVARCHAR(50),
    size NVARCHAR(50)
);

-- Dữ liệu mẫu: Users
INSERT INTO Users (username, password, email, fullname, phone, address, role, active) VALUES
('admin', '$2a$10$Ik0iUEmXCrNQMrd9W.GnUOfRfRblOFsy.P39ep6J.Ukkgga5pyLlu', 'admin@example.com', 'Admin User', '0123456789', 'Hanoi, Vietnam', 'ADMIN', 1),
('user1', '$2a$10$3mH89ZBFAZ7PG2Y1XVvMN.wlr6YZV2b4rzqinrOZHLTWLLfP8Lwue', 'user1@example.com', 'User One', '0987654321', 'HCMC, Vietnam', 'USER', 1),
('user2', '$2a$10$0VQfpeUQ/bMYxRikNTjd9ON.xcKpi87IDenfrOkf8gvPwugZxb2MK', 'user2@example.com', 'User Two', '0912345678', 'Da Nang, Vietnam', 'USER', 1),
('staff1', '$2a$10$1jcmwNnbqe4Nk52yC8fPe.8xIemRZG2I/r97xxrtBjnx1WxxabgmC', 'staff1@example.com', 'Staff One', '0931234567', 'Hanoi, Vietnam', 'STAFF', 1);


INSERT INTO Categories (name) VALUES
(N'ÁO THUN'),
(N'ÁO KHOÁC'),
(N'QUẦN SHORT'),
(N'QUẦN JEAN'),
(N'VỚ')

INSERT INTO Products (name, description, category_id) VALUES
(N'ÁO THUN LENUP STAR', 
N'Chất liệu: cotton 100% nguyên bản – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 4),
(N'ÁO THUN LENUP L', 
N'Chất liệu: cotton 100% nguyên bản – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 4),
(N'ÁO THUN LENUP SNAKE ROSE', 
N'Chất liệu: cotton 100% nguyên bản – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 4),
(N'ÁO THUN LENUP SKELETON', 
N'Chất liệu: cotton 100% nguyên bản – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 4),
(N'ÁO THUN LENUP TOPIER', 
N'Chất liệu: cotton 100% nguyên bản – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 4),
(N'ÁO KHOÁC LENUP WHITE', 
N'Chất liệu: Vải dù – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 5),
(N'ÁO KHOÁC LENUP BLACK', 
N'Chất liệu: Vải dù – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 5),
(N'ÁO KHOÁC LENUP GRAY', 
N'Chất liệu: Vải dù – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 5),
(N'QUẦN SHORT LENUP WHITE', 
N'Chất liệu: Vải thun – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 6),
(N'QUẦN SHORT LENUP BLACK', 
N'Chất liệu: Vải thun – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 6),
(N'QUẦN JEAN LENUP WHITE', 
N'Chất liệu: Vải jean – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 7),
(N'QUẦN JEAN LENUP BLACK', 
N'Chất liệu: Vải jean – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 7),
(N'QUẦN JEAN LENUP GRAY', 
N'Chất liệu: Vải jean – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 7),
(N'VỚ LENUP WHITE', 
N'Chất liệu: Vải dù – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 8),
(N'VỚ LENUP BLACK', 
N'Chất liệu: Vải dù – mềm mại như làn da, thoáng mát cả ngày dài.
250 GSM chuẩn chỉnh – giữ form đẹp, vững vàng theo từng chuyển động.', 8)

INSERT INTO ProductVariants (product_id, size, color, stock, price) VALUES
(18, 'M', N'Đen', '50', '179000'),
(18, 'M', N'Trắng', '50', '179000'),
(18, 'L', N'Đen', '50', '179000'),
(18, 'L', N'Trắng', '50', '179000'),
(18, 'XL', N'Đen', '50', '179000'),
(18, 'XL', N'Trắng', '50', '179000'),
(17, 'M', N'Đen', '50', '179000'),
(17, 'M', N'Trắng', '50', '179000'),
(17, 'L', N'Đen', '50', '179000'),
(17, 'L', N'Trắng', '50', '179000'),
(17, 'XL', N'Đen', '50', '179000'),
(17, 'XL', N'Trắng', '50', '179000'),
(19, 'M', N'Đen', '50', '179000'),
(19, 'L', N'Đen', '50', '179000'),
(19, 'XL', N'Đen', '50', '179000'),
(20, 'M', N'Đen', '50', '179000'),
(20, 'L', N'Đen', '50', '179000'),
(20, 'XL', N'Đen', '50', '179000'),
(21, 'M', N'Trắng', '50', '179000'),
(21, 'L', N'Trắng', '50', '179000'),
(21, 'XL', N'Trắng', '50', '179000'),
(22, 'M', N'Trắng', '50', '199000'),
(22, 'L', N'Trắng', '50', '199000'),
(22, 'XL', N'Trắng', '50', '199000'),
(22, 'M', N'Đen', '50', '199000'),
(22, 'L', N'Đen', '50', '199000'),
(22, 'XL', N'Đen', '50', '199000'),
(24, 'M', N'Xám', '50', '199000'),
(24, 'L', N'Xám', '50', '199000'),
(24, 'XL', N'Xám', '50', '199000')

INSERT INTO ProductImages (product_id, variant_id, image_url) VALUES
(18, NULL, N'/uploads/L_den.jpg'),
(18, 10, N'/uploads/L_den_nguoimau.jpg'),
(18, 15, N'/uploads/L_trang.jpg'),
(18, 18, N'/uploads/L_trangden.jpg'),
(17, NULL, N'/uploads/star_den.jpg'),
(17, 20, N'/uploads/star_trang.jpg'),
(17, 21, N'/uploads/star_den3d_sau.jpg'),
(17, 22, N'/uploads/star_trang1.jpg'),
(17, 23, N'/uploads/star_trangden.jpg'),
(17, 24, N'/uploads/star_trangden1.jpg'),
(19, NULL, N'/uploads/snakerose_den.jpg'),
(19, 26, N'/uploads/snakerose_den.jpg'),
(20, NULL, N'/uploads/snakerose_den.jpg'),
(20, NULL, N'/uploads/snakerose_den.jpg')
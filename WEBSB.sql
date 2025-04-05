USE master
 CREATE DATABASE WEBSB
 USE WEBSB

 CREATE TABLE Users (
    id INT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(50) UNIQUE NOT NULL,
    password NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    fullname NVARCHAR(100),
    phone NVARCHAR(10),
    address NVARCHAR(255),
    role NVARCHAR(20) CHECK (role IN ('USER', 'ADMIN')) DEFAULT 'USER',
    active BIT DEFAULT 1
);

CREATE TABLE Categories (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE Products (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(500),
    price DECIMAL(18,2) NOT NULL,
    stock INT NOT NULL,
    image NVARCHAR(255),
    category_id INT FOREIGN KEY REFERENCES Categories(id)
);

CREATE TABLE Orderss (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,      
    fullname NVARCHAR(255) NOT NULL,         
    phone NVARCHAR(20) NOT NULL,                
    address NVARCHAR(255) NOT NULL,            
    payment_method NVARCHAR(50) NOT NULL,      
    total_price FLOAT NOT NULL,                 
    order_date DATETIME DEFAULT GETDATE()       
);
ALTER TABLE Orderss ADD status VARCHAR(255);

CREATE TABLE OrderDetails (
    id INT PRIMARY KEY IDENTITY(1,1),
    order_id INT FOREIGN KEY REFERENCES Orders(id),
    product_id INT FOREIGN KEY REFERENCES Products(id),
    quantity INT NOT NULL,
    price DECIMAL(18,2) NOT NULL
);

-- Tạo bảng Carts
CREATE TABLE Carts (
    id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT FOREIGN KEY REFERENCES Users(id),  -- Liên kết với bảng Users
    created_at DATETIME DEFAULT GETDATE(),         -- Thời gian tạo giỏ hàng
    updated_at DATETIME DEFAULT GETDATE()          -- Thời gian cập nhật giỏ hàng
);

-- Tạo bảng CartItems
CREATE TABLE cart_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,   -- Tạo cột id tự tăng
    product_id BIGINT NOT NULL,              -- Cột productId, không được NULL
    product_name NVARCHAR(255) NOT NULL,     -- Cột productName, không được NULL
    price FLOAT NOT NULL,                   -- Cột price, không được NULL
    quantity INT NOT NULL,                  -- Cột quantity, không được NULL
    image_url NVARCHAR(255) NOT NULL,        -- Cột imageUrl, không được NULL
    order_id BIGINT NOT NULL,               -- Cột order_id, là khóa ngoại tham chiếu đến bảng Order
    FOREIGN KEY (order_id) REFERENCES Orderss(id)  -- Ràng buộc khóa ngoại tới bảng Orders
);


-- Thêm dữ liệu mẫu vào bảng Carts
INSERT INTO Carts (user_id) VALUES
(1), -- Giỏ hàng cho người dùng admin
(2); -- Giỏ hàng cho người dùng user1

-- Thêm dữ liệu mẫu vào bảng CartItems
INSERT INTO CartItems (cart_id, product_id, quantity, total_price) VALUES
(1, 1, 2, 189000 * 2),  -- Giỏ hàng của admin có 2 sản phẩm "ÁO THUN LENUP STAR"
(1, 3, 1, 200000 * 1),  -- Giỏ hàng của admin có 1 sản phẩm "ÁO KHOÁC LENUP MÙA ĐÔNG"
(2, 4, 1, 189000 * 1);  -- Giỏ hàng của user1 có 1 sản phẩm "QUẦN LENUP"

-- Thêm dữ liệu vào bảng Users
INSERT INTO Users (username, password, email, fullname, phone, address, role, active) VALUES
('admin', '123456', 'admin@example.com', 'Admin User', '0123456789', 'Hanoi, Vietnam', 'ADMIN', 1),
('user1', 'password1', 'user1@example.com', 'User One', '0987654321', 'HCMC, Vietnam', 'USER', 1),
('user2', 'password2', 'user2@example.com', 'User Two', '0912345678', 'Da Nang, Vietnam', 'USER', 1);

-- Thêm dữ liệu vào bảng Categories
INSERT INTO Categories (name) VALUES
(N'ÁO THUN'),
(N'ÁO KHOÁC'),
(N'QUẦN');

-- Thêm dữ liệu vào bảng Products
INSERT INTO Products (name, description, price, stock, image, category_id) VALUES
(N'ÁO THUN LENUP STAR', N'Áo đẹp lắm', 189000, 10, 'star.jpg', 1),
(N'ÁO THUN LENUP L', N'Áo đẹp lắm', 189000, 5, 'l.jpg', 1),
(N'ÁO KHOÁC LENUP MÙA ĐÔNG', N'Áo đẹp lắm', 200000, 50, 'aokhoac.jpg', 2),
(N'QUẦN LENUP', N'Quần đẹp lắm', 189000, 7, 'quan.jpg', 3);

-- Thêm dữ liệu vào bảng Orders
INSERT INTO Orders (user_id, status, total) VALUES
(1, 'CONFIRMED', 12000000),
(2, 'PENDING', 5000000);

-- Thêm dữ liệu vào bảng OrderDetails
INSERT INTO OrderDetails (order_id, product_id, quantity, price) VALUES
(1, 1, 1, 189000),
(2, 4, 1, 189000);


-- Tạo database
USE master;
DROP DATABASE IF EXISTS LENUP;
CREATE DATABASE LENUP;
COLLATE Vietnamese_CI_AS;
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
    active BIT DEFAULT 1
);

-- Bảng Categories
CREATE TABLE Categories (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) UNIQUE NOT NULL
);

-- Bảng Products (xóa cột image)
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

-- Bảng ProductImages (thêm variant_id)
CREATE TABLE ProductImages (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    product_id BIGINT FOREIGN KEY REFERENCES Products(id),
    variant_id BIGINT FOREIGN KEY REFERENCES ProductVariants(id),
    image_url NVARCHAR(255) NOT NULL
);

-- Bảng Orders (đổi tên từ Orderss)
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
('admin', '123456', 'admin@example.com', 'Admin User', '0123456789', 'Hanoi, Vietnam', 'ADMIN', 1),
('user1', 'password1', 'user1@example.com', 'User One', '0987654321', 'HCMC, Vietnam', 'USER', 1),
('user2', 'password2', 'user2@example.com', 'User Two', '0912345678', 'Da Nang, Vietnam', 'USER', 1),
('staff1', 'password123', 'staff1@example.com', 'Staff One', '0931234567', 'Hanoi, Vietnam', 'STAFF', 1);



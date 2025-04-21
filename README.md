# Grocery Store Inventory Management App

A modern Android-based inventory management system for grocery retailers, offering seamless tracking, updating, and organization of store inventory through a simple and intuitive user interface.

## Overview

This app allows store staff to:

- Add new products
- Search inventory by SKU or product name
-  Scan out products for waste or reclamation
-  Adjust product stock levels
-  Test SQL Server connectivity

## Tech Stack

| Layer         | Technology             |
|---------------|------------------------|
| Language      | Kotlin (Android)       |
| UI Framework  | Jetpack Compose        |
| Database      | Microsoft SQL Server   |
| Connectivity  | JDBC (JTDS driver)     |

## Features

- **Add Product:** Enter details such as SKU, category, price, and stock.
- **Search Product:** View stock levels, SKU, and UPC.
- **Product Details:** Adjust stock (increase/decrease), delete items.
- **Scan Out Products:** Deduct scanned items from stock and confirm batch changes.
- **Connectivity Test:** Built-in tool to verify SQL Server access from the app.

## SQL Server Schema

sql
CREATE TABLE Products (
    ProductID INT IDENTITY(1,1) PRIMARY KEY,
    ProductName NVARCHAR(255) NOT NULL,
    SKU NVARCHAR(50) UNIQUE NOT NULL,
    Category NVARCHAR(100),
    Quantity INT DEFAULT 0,
    Price DECIMAL(10,2),
    Supplier NVARCHAR(255),
    Image VARBINARY(MAX)
);

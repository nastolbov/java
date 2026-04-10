CREATE TABLE IF NOT EXISTS Genre (
    GenreID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Description VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS Developer (
    DeveloperID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(200) NOT NULL,
    ContactName VARCHAR(150),
    PhoneNumber VARCHAR(20),
    Email VARCHAR(100),
    Address VARCHAR(300)
);

CREATE TABLE IF NOT EXISTS Game (
    GameID INT PRIMARY KEY,
    Title VARCHAR(200) NOT NULL,
    Description VARCHAR(500),
    Price DOUBLE NOT NULL,
    StockQuantity INT NOT NULL,
    GenreID INT,
    DeveloperID INT,
    FOREIGN KEY (GenreID) REFERENCES Genre(GenreID),
    FOREIGN KEY (DeveloperID) REFERENCES Developer(DeveloperID)
);

CREATE TABLE IF NOT EXISTS Customer (
    CustomerID INT PRIMARY KEY,
    Name VARCHAR(200) NOT NULL,
    Email VARCHAR(100),
    PhoneNumber VARCHAR(20),
    Address VARCHAR(300),
    RegistrationDate DATE
);

CREATE TABLE IF NOT EXISTS Purchase (
    PurchaseID INT AUTO_INCREMENT PRIMARY KEY,
    CustomerID INT,
    PurchaseDate DATE,
    TotalAmount DOUBLE,
    Status VARCHAR(50),
    GameID INT,
    Count INT,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
    FOREIGN KEY (GameID) REFERENCES Game(GameID)
);

-- Жанры
INSERT INTO Genre (GenreID, Name, Description) VALUES (1, 'RPG', 'Ролевые игры с развитием персонажа');
INSERT INTO Genre (GenreID, Name, Description) VALUES (2, 'Шутер', 'Игры от первого или третьего лица с перестрелками');
INSERT INTO Genre (GenreID, Name, Description) VALUES (3, 'Стратегия', 'Стратегические игры в реальном времени и пошаговые');
INSERT INTO Genre (GenreID, Name, Description) VALUES (4, 'Приключения', 'Приключенческие игры с исследованием мира');
INSERT INTO Genre (GenreID, Name, Description) VALUES (5, 'Спорт', 'Спортивные симуляторы');

-- Разработчики
INSERT INTO Developer (DeveloperID, Name, ContactName, PhoneNumber, Email, Address) VALUES (1, 'CD Projekt Red', 'Адам Бадовски', '+48221234567', 'contact@cdprojektred.com', 'Варшава, Польша');
INSERT INTO Developer (DeveloperID, Name, ContactName, PhoneNumber, Email, Address) VALUES (2, 'Valve Corporation', 'Гейб Ньюэлл', '+14255551234', 'info@valvesoftware.com', 'Белвью, Вашингтон, США');
INSERT INTO Developer (DeveloperID, Name, ContactName, PhoneNumber, Email, Address) VALUES (3, 'Paradox Interactive', 'Фредрик Вестер', '+46812345678', 'info@paradoxplaza.com', 'Стокгольм, Швеция');
INSERT INTO Developer (DeveloperID, Name, ContactName, PhoneNumber, Email, Address) VALUES (4, 'Rockstar Games', 'Сэм Хаузер', '+12125551234', 'contact@rockstargames.com', 'Нью-Йорк, США');
INSERT INTO Developer (DeveloperID, Name, ContactName, PhoneNumber, Email, Address) VALUES (5, 'Electronic Arts', 'Эндрю Уилсон', '+16505551234', 'info@ea.com', 'Редвуд-Сити, Калифорния, США');

-- Игры
INSERT INTO Game (GameID, Title, Description, Price, StockQuantity, GenreID, DeveloperID) VALUES (1, 'The Witcher 3: Wild Hunt', 'Ролевая игра в открытом мире по вселенной Ведьмака', 1499.99, 120, 1, 1);
INSERT INTO Game (GameID, Title, Description, Price, StockQuantity, GenreID, DeveloperID) VALUES (2, 'Cyberpunk 2077', 'RPG в открытом мире в жанре киберпанк', 1999.99, 85, 1, 1);
INSERT INTO Game (GameID, Title, Description, Price, StockQuantity, GenreID, DeveloperID) VALUES (3, 'Counter-Strike 2', 'Командный тактический шутер', 0.00, 999, 2, 2);
INSERT INTO Game (GameID, Title, Description, Price, StockQuantity, GenreID, DeveloperID) VALUES (4, 'Half-Life: Alyx', 'VR шутер от первого лица', 2199.99, 45, 2, 2);
INSERT INTO Game (GameID, Title, Description, Price, StockQuantity, GenreID, DeveloperID) VALUES (5, 'Crusader Kings III', 'Глобальная стратегия средневековья', 1799.99, 60, 3, 3);
INSERT INTO Game (GameID, Title, Description, Price, StockQuantity, GenreID, DeveloperID) VALUES (6, 'Grand Theft Auto V', 'Экшен-приключение в открытом мире', 999.99, 200, 4, 4);
INSERT INTO Game (GameID, Title, Description, Price, StockQuantity, GenreID, DeveloperID) VALUES (7, 'Red Dead Redemption 2', 'Вестерн-приключение в открытом мире', 2499.99, 75, 4, 4);
INSERT INTO Game (GameID, Title, Description, Price, StockQuantity, GenreID, DeveloperID) VALUES (8, 'FIFA 25', 'Футбольный симулятор', 3499.99, 150, 5, 5);
INSERT INTO Game (GameID, Title, Description, Price, StockQuantity, GenreID, DeveloperID) VALUES (9, 'Stellaris', 'Космическая глобальная стратегия', 1299.99, 40, 3, 3);
INSERT INTO Game (GameID, Title, Description, Price, StockQuantity, GenreID, DeveloperID) VALUES (10, 'Portal 2', 'Головоломка от первого лица с порталами', 699.99, 90, 4, 2);

-- Покупатели
INSERT INTO Customer (CustomerID, Name, Email, PhoneNumber, Address, RegistrationDate) VALUES (1, 'Иванов Иван Иванович', 'ivanov@mail.ru', '+79001234567', 'Москва, ул. Ленина 10', '2025-01-15');
INSERT INTO Customer (CustomerID, Name, Email, PhoneNumber, Address, RegistrationDate) VALUES (2, 'Петров Пётр Петрович', 'petrov@gmail.com', '+79012345678', 'Санкт-Петербург, Невский пр. 25', '2025-02-20');
INSERT INTO Customer (CustomerID, Name, Email, PhoneNumber, Address, RegistrationDate) VALUES (3, 'Сидорова Анна Михайловна', 'sidorova@yandex.ru', '+79023456789', 'Казань, ул. Баумана 5', '2025-03-10');
INSERT INTO Customer (CustomerID, Name, Email, PhoneNumber, Address, RegistrationDate) VALUES (4, 'Козлов Дмитрий Сергеевич', 'kozlov@mail.ru', '+79034567890', 'Новосибирск, ул. Красный проспект 50', '2025-04-05');
INSERT INTO Customer (CustomerID, Name, Email, PhoneNumber, Address, RegistrationDate) VALUES (5, 'Морозова Елена Александровна', 'morozova@inbox.ru', '+79045678901', 'Екатеринбург, ул. Мира 15', '2025-05-12');

-- Покупки
INSERT INTO Purchase (PurchaseID, CustomerID, PurchaseDate, TotalAmount, Status, GameID, Count) VALUES (1, 1, '2025-01-20', 1499.99, 'Завершён', 1, 1);
INSERT INTO Purchase (PurchaseID, CustomerID, PurchaseDate, TotalAmount, Status, GameID, Count) VALUES (2, 1, '2025-02-14', 1999.99, 'Завершён', 2, 1);
INSERT INTO Purchase (PurchaseID, CustomerID, PurchaseDate, TotalAmount, Status, GameID, Count) VALUES (3, 2, '2025-02-28', 999.99, 'Завершён', 6, 1);
INSERT INTO Purchase (PurchaseID, CustomerID, PurchaseDate, TotalAmount, Status, GameID, Count) VALUES (4, 2, '2025-03-15', 2499.99, 'Завершён', 7, 1);
INSERT INTO Purchase (PurchaseID, CustomerID, PurchaseDate, TotalAmount, Status, GameID, Count) VALUES (5, 3, '2025-03-20', 3499.99, 'Завершён', 8, 1);
INSERT INTO Purchase (PurchaseID, CustomerID, PurchaseDate, TotalAmount, Status, GameID, Count) VALUES (6, 3, '2025-04-01', 1799.99, 'Завершён', 5, 1);
INSERT INTO Purchase (PurchaseID, CustomerID, PurchaseDate, TotalAmount, Status, GameID, Count) VALUES (7, 4, '2025-04-10', 2199.99, 'Завершён', 4, 1);
INSERT INTO Purchase (PurchaseID, CustomerID, PurchaseDate, TotalAmount, Status, GameID, Count) VALUES (8, 4, '2025-05-05', 699.99, 'В обработке', 10, 1);
INSERT INTO Purchase (PurchaseID, CustomerID, PurchaseDate, TotalAmount, Status, GameID, Count) VALUES (9, 5, '2025-05-15', 1299.99, 'Завершён', 9, 1);
INSERT INTO Purchase (PurchaseID, CustomerID, PurchaseDate, TotalAmount, Status, GameID, Count) VALUES (10, 5, '2025-06-01', 4999.98, 'Завершён', 2, 2);
INSERT INTO Purchase (PurchaseID, CustomerID, PurchaseDate, TotalAmount, Status, GameID, Count) VALUES (11, 1, '2025-06-10', 1799.99, 'В обработке', 5, 1);
INSERT INTO Purchase (PurchaseID, CustomerID, PurchaseDate, TotalAmount, Status, GameID, Count) VALUES (12, 3, '2025-06-15', 6999.98, 'Завершён', 8, 2);

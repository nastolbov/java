# Гайд по защите — Магазин компьютерных игр

---

## Навигация

| | |
|---|---|
| [⚡ Быстрый старт](#быстрый-старт) | Запустить за 2 минуты |
| [🎬 Демонстрация](#демонстрация) | Что показывать и что говорить |
| [🏗 Архитектура](#архитектура) | Как устроена программа |
| [📁 Разбор по классам](#разбор-по-классам) | Каждый класс — код и объяснение |
| [🗄 База данных](#база-данных) | Таблицы, связи, каскадное удаление |
| [⚙️ Как работает CRUD](#как-работает-crud) | Пошагово от кнопки до SQL |
| [🔍 Фильтры и поиск](#фильтры-и-поиск) | Stream API и FieldFilter |
| [📊 Дашборд](#дашборд) | Метрики, графики, SVG диаграмма |
| [❓ Топ-10 вопросов](#топ-10-вопросов) | Вопросы и готовые ответы |

---

## Быстрый старт

[↑ Наверх](#навигация)

```bash
cd ~/Desktop/java
git pull origin claude/coursework-documentation-NLjye
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn spring-boot:run
```

Ждёшь строку `Started Application` → браузер откроется сам на **http://localhost:8080**

> ⚠️ Java должна быть 17-я. Если ошибка `Unsupported class file major version` — значит запустилось на Java 25. Пропиши `export JAVA_HOME=...` в `~/.zshrc` один раз чтобы не повторять.

---

## Демонстрация

[↑ Наверх](#навигация)

### Шаг 1 — Игры
- Показать таблицу: ID, название, цена в рублях, жанр, разработчик
- **Добавить** → поля заполняются, жанр и разработчик выбираются из выпадашки → Сохранить
- Выбрать строку → **Изменить** → поменять цену → Сохранить
- Выбрать строку → **Удалить** → подтвердить

> *«Каталог игр. У каждой — название, цена, количество на складе, жанр и разработчик. Жанр и разработчик — внешние ключи на справочные таблицы, поэтому выбираются из списка»*

### Шаг 2 — Покупки
- Показать таблицу: покупатель по имени, игра по названию, дата, сумма, цветной статус
- **Добавить** → выбрать покупателя и игру из выпадашек, цена подставляется автоматически
- Показать что при изменении количества — сумма пересчитывается

> *«Каждая покупка связывает покупателя с игрой. Статус выделен цветом: зелёный — завершено, оранжевый — в обработке, красный — отменено»*

### Шаг 3 — Покупатели, Разработчики, Жанры
- Быстро показать данные, добавить одну запись

> *«Справочные таблицы. Связаны через внешние ключи с таблицей Game»*

### Шаг 4 — Фильтры
- В таблице Игры: вписать часть названия в поиск → список сужается в реальном времени
- Выбрать жанр → только игры этого жанра
- Ввести диапазон цен → комбинируется с остальными фильтрами

> *«Все фильтры работают одновременно — это Stream API в Java: несколько .filter() вызовов на одном конвейере»*

### Шаг 5 — Дашборд ⭐
- Показать 6 карточек: выручка только по оплаченным, завершено, в обработке, отменено, покупатели, игры
- Показать **график по месяцам** (горизонтальные полосы, только завершённые)
- Показать **donut-диаграмму** с процентами внутри секторов
- Показать **топ-5 игр** по выручке
- Удалить запись → перейти на дашборд → данные обновились

> *«Дашборд учитывает статус покупки. Выручка считается только по завершённым и оплаченным заказам. При каждом открытии вкладки данные заново читаются из базы»*

---

## Архитектура

[↑ Наверх](#навигация)

### Три уровня

```
┌──────────────────────────────────────────┐
│  VIEW (Vaadin)                           │  ← интерфейс в браузере
│  MainView, DashboardView, *DialogManager │
├──────────────────────────────────────────┤
│  SERVICE (Spring + JDBC)                 │  ← SQL-запросы к базе
│  GameService, PurchaseService, ...       │
├──────────────────────────────────────────┤
│  DATABASE (H2 in-memory)                 │  ← данные в памяти
│  schema.sql → data.sql                   │
└──────────────────────────────────────────┘
```

### Порядок запуска

```
Application.java:13    SpringApplication.run(...)
        ↓
application.properties:1   spring.datasource.url=jdbc:h2:mem:computer_games
        ↓
application.properties:8   schema.sql  → CREATE TABLE Game, Purchase...
application.properties:9   data.sql    → INSERT INTO (тестовые данные)
        ↓
Spring создаёт @Service бины: GameService, PurchaseService...
        ↓
Vaadin открывает MainView → загружает данные через сервисы
        ↓
http://localhost:8080 — готово
```

### Что происходит при нажатии «Добавить игру»

```
Кнопка addButton             MainView.java:331
        ↓
openAddGameDialog()          GameDialogManager.java:35
        ↓
Пользователь заполнил форму, нажал «Сохранить»
        ↓
Валидация полей              GameDialogManager.java:57
Автогенерация ID             GameDialogManager.java:61  max(ID) + 1
new Game(newID, title, ...)  GameDialogManager.java:62
        ↓
gameService.createGame(game) GameDialogManager.java:73
        ↓
PreparedStatement INSERT      GameService.java:44-56
stmt.executeUpdate()         GameService.java:56  — SQL уходит в H2
        ↓
games.add(game)              GameDialogManager.java:73
grid.setItems(games)         GameDialogManager.java:74  — таблица обновлена
```

---

## Разбор по классам

[↑ Наверх](#навигация)

### Application.java — точка входа

**Файл:** `src/main/java/org/triangle/Application.java`

```java
// Application.java:8-14
@SpringBootApplication   // говорит Spring: «сканируй всё и запускай»
@Theme("default")        // подключает CSS из frontend/themes/default/
public class Application implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);  // строка 13 — запуск сервера
    }
}
```

**Что делает:** одна строка запускает встроенный Tomcat, поднимает H2, создаёт все `@Service`-бины через DI, открывает браузер на localhost:8080.

---

### Game.java — модель данных

**Файл:** `src/main/java/org/triangle/base/Class/Model/Game.java`

```java
// Game.java:3-20 — POJO-класс: просто коробка для полей, без логики
public class Game {
    private int gameID;          // строка 4
    private String title;        // строка 5
    private double price;        // строка 7
    private int stockQuantity;   // строка 8
    private int genreID;         // строка 9  — ID жанра (внешний ключ)
    private int developerID;     // строка 10 — ID разработчика (внешний ключ)

    public Game(int gameID, String title, ...) { // строка 12 — конструктор
        this.gameID = gameID; ...
    }
    // геттеры: строки 22-76, сеттеры: строки 26-75
}
```

**Что делает:** хранит данные одной игры. Никакой логики — только поля + геттеры/сеттеры. Такая же структура у `Customer`, `Purchase`, `Genre`, `Developer`.

---

### GameService.java — работа с базой данных

**Файл:** `src/main/java/org/triangle/base/ui/service/GameService.java`

```java
// GameService.java:10-12
@Service  // строка 10 — Spring создаёт этот объект и передаёт в MainView через конструктор
public class GameService {
    private static final String URL = "jdbc:h2:mem:computer_games"; // строка 12
```

**Чтение — `getGames()`:**
```java
// GameService.java:16-40
public List<Game> getGames() {
    String query = "SELECT * FROM Game";   // строка 18
    Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); // строка 20
    ResultSet rs = stmt.executeQuery(query);
    while (rs.next()) {                    // строка 24 — перебираем строки результата
        Game g = new Game(
            rs.getInt("GameID"),           // строка 26 — берём значение поля
            rs.getString("Title"),         // строка 27
            rs.getDouble("Price"),         // строка 29
            ...
        );
        games.add(g);                      // строка 34
    }
    return games;                          // строка 39
}
```

**Добавление — `createGame()`:**
```java
// GameService.java:42-58
String insertSQL = """
    INSERT INTO Game (GameID, Title, Description, Price, StockQuantity, GenreID, DeveloperID)
    VALUES (?, ?, ?, ?, ?, ?, ?)
    """;  // строки 44-47 — знаки ? защищают от SQL-инъекций (PreparedStatement)
PreparedStatement stmt = conn.prepareStatement(insertSQL);
stmt.setInt(1, game.getGameID());    // строка 49 — подставляем значения по позиции
stmt.setString(2, game.getTitle()); // строка 50
stmt.executeUpdate();               // строка 56 — отправляем SQL в базу
```

**Удаление — `deleteGame()`:**
```java
// GameService.java:87-94
String deleteSQL = "DELETE FROM Game WHERE GameID = ?"; // строка 89
stmt.setInt(1, gameId);   // строка 91
stmt.executeUpdate();     // строка 92
// schema.sql:46 — ON DELETE CASCADE автоматически удалит покупки этой игры
```

---

### MainView.java — главный экран

**Файл:** `src/main/java/org/triangle/base/ui/view/MainView.java`

**Загрузка данных при старте:**
```java
// MainView.java:82-85
List<Game> games             = gameService.getGames();        // строка 82
List<Purchase> purchases     = purchaseService.getPurchases(); // строка 83
List<Customer> customers     = customerService.getCustomers(); // строка 84
List<Developer> developers   = developerService.getDevelopers(); // строка 85
List<Genre> genres           = genreService.getGenres();      // строка 86 (приблизительно)
```

**Обновление при переключении вкладки:**
```java
// MainView.java:386-419 — слушатель смены вкладки
tabs.addSelectedChangeListener(event -> {           // строка 386
    Tab selectedTab = event.getSelectedTab();        // строка 388
    if (selectedTab == tab1) {
        games.clear();                              // строка 392 — стираем устаревшее
        games.addAll(gameService.getGames());       // строка 393 — читаем свежее из БД
        grid.setItems(games);                       // строка 394 — перерисовываем таблицу
    } else if (selectedTab == dashboardTab) {
        dashboardContent.removeAll();
        dashboardContent.add(new DashboardView(    // строка 413 — пересоздаём дашборд
            purchaseService.getPurchases(), ...    // строка 414 — свежие данные
        ));
    }
});
```

**Почему `clear()` + `addAll()`, а не `games = service.getGames()`?**  
Потому что `GameDialogManager`, фильтры и другие объекты держат ссылку на тот же список. Если создать новый объект `games =`, они продолжат видеть старый список. `clear()` + `addAll()` изменяет содержимое того же объекта — все видят обновление.

**Многокритериальный фильтр для игр:**
```java
// MainView.java:119-138
Runnable applyGameFilter = () -> {
    String q    = gameTitleSearch.getValue().toLowerCase();
    Genre genre = gameGenreFilter.getValue();
    Double minP = minPriceField.getValue();
    Double maxP = maxPriceField.getValue();
    grid.setItems(games.stream()
        .filter(g -> q.isEmpty() || g.getTitle().toLowerCase().contains(q)) // поиск по подстроке
        .filter(g -> genre == null || g.getGenreID() == genre.getGenreID()) // по жанру
        .filter(g -> minP == null || g.getPrice() >= minP)                  // цена от
        .filter(g -> maxP == null || g.getPrice() <= maxP)                  // цена до
        .collect(Collectors.toList()));
};
gameTitleSearch.addValueChangeListener(e -> applyGameFilter.run()); // строка 136
```

---

### GameDialogManager.java — диалоги для игр

**Файл:** `src/main/java/org/triangle/base/ui/view/Dialog/GameDialogManager.java`

```java
// GameDialogManager.java:27-33 — конструктор: получает сервис, таблицу и справочники
public GameDialogManager(GameService gameService, Grid<Game> grid,
                         List<Genre> genres, List<Developer> developers) {

// GameDialogManager.java:44-50 — ComboBox вместо числового поля
ComboBox<Genre> genreBox = new ComboBox<>("Жанр");
genreBox.setItems(genres);                         // строка 45 — наполняем список
genreBox.setItemLabelGenerator(Genre::getName);    // строка 46 — показываем getName()

// GameDialogManager.java:61 — ID = максимальный существующий + 1
int newID = games.stream().mapToInt(Game::getGameID).max().orElse(0) + 1;
```

**При редактировании — предзаполнение текущим жанром:**
```java
// GameDialogManager.java:97-100
genres.stream()
    .filter(g -> g.getGenreID() == selected.getGenreID()) // найти текущий жанр игры
    .findFirst()
    .ifPresent(genreBox::setValue);  // выбрать его в ComboBox
```

---

### FieldFilter.java — универсальный фильтр

**Файл:** `src/main/java/org/triangle/base/ui/Components/FieldFilter.java`

```java
// FieldFilter.java:7 — обобщённый класс
// T = тип объекта (например Game), V = тип поля (например String — название)
public class FieldFilter<T, V> {

// FieldFilter.java:12-19 — при создании собирает уникальные значения поля
public FieldFilter(List<T> items, Function<T, V> fieldExtractor) {
    this.availableValues = items.stream()    // строка 15 — Stream-конвейер
            .map(fieldExtractor)             // строка 16 — применяем функцию: Game → title
            .filter(Objects::nonNull)        // строка 17 — убрать null-значения
            .collect(Collectors.toSet());    // строка 18 — Set автоматически убирает дубли
}

// FieldFilter.java:27-34 — фильтрация по конкретному значению
public List<T> filterByValue(V value) {
    return items.stream()
            .filter(item -> Objects.equals(fieldExtractor.apply(item), value))
            .collect(Collectors.toList());
}
```

**Пример создания:**
```java
// MainView.java:178
new FieldFilter<>(customers, Customer::getName)
//     T=Customer, V=String
//     Customer::getName = метод-ссылка, равносильно c -> c.getName()
```

---

### DashboardView.java — аналитика

**Файл:** `src/main/java/org/triangle/base/ui/view/DashboardView.java`

**Разбивка по статусам:**
```java
// DashboardView.java:23-24 — константы для статусов
private static final Set<String> PAID    = Set.of("Завершён", "Оплачено");
private static final Set<String> PENDING = Set.of("В обработке", "Ожидание");

// DashboardView.java:51-57
List<Purchase> completed = filter(PAID);    // только оплаченные
List<Purchase> pending   = filter(PENDING); // только ожидающие
double revenue = completed.stream()         // строка 57
    .mapToDouble(Purchase::getTotalAmount)  // достать TotalAmount из каждой покупки
    .sum();                                 // просуммировать
```

**Форматирование суммы по-русски:**
```java
// DashboardView.java:377-383
private String money(double v) {
    String raw = String.format(Locale.US, "%,.2f", v); // "26,300.00" (US-формат)
    String[] parts = raw.split("\\.");
    String intPart  = parts[0].replace(",", " ");       // "26 300" (пробел вместо запятой)
    return intPart + "," + parts[1] + " ₽";             // "26 300,00 ₽" (русский формат)
}
```

**SVG donut-диаграмма без библиотек:**
```java
// DashboardView.java:330-360
int cx = 120, cy = 120, r = 100, ri = 52; // центр, внешний и внутренний радиус
double midR = (r + ri) / 2.0;             // радиус середины кольца = 76

for (каждый жанр) {
    double sweep = pct * 360.0;   // угол сектора = доля * 360°
    // вычисляем 4 точки дуги через sin/cos
    double x1o = cx + r  * Math.cos(Math.toRadians(start)); // внешняя точка начала
    double x2o = cx + r  * Math.cos(Math.toRadians(end));   // внешняя точка конца
    double x1i = cx + ri * Math.cos(Math.toRadians(end));   // внутренняя точка конца
    double x2i = cx + ri * Math.cos(Math.toRadians(start)); // внутренняя точка начала

    // SVG путь: M=начало, A=дуга, L=линия, Z=закрыть
    "<path d='M x1o y1o  A r r 0 largeArc 1 x2o y2o  L x1i y1i  A ri ri 0 largeArc 0 x2i y2i  Z'/>"

    // строка 348 — процент только если сектор >= 7% (есть место)
    if (pct >= 0.07) {
        double mid = start + sweep / 2.0;          // угол середины сектора
        double tx = cx + midR * Math.cos(mid);     // X подписи
        double ty = cy + midR * Math.sin(mid);     // Y подписи
        // добавляем <text> с белым процентом
    }
}
// Вставка SVG в Vaadin-компонент:
container.getElement().setProperty("innerHTML", svgString); // строка 362
```

---

## База данных

[↑ Наверх](#навигация)

### Структура таблиц

```
schema.sql:1-5    Genre      — жанры (RPG, Шутер, Стратегия...)
schema.sql:7-14   Developer  — разработчики (Valve, CD Projekt Red...)
schema.sql:16-26  Game       — игры, ссылается на Genre и Developer
schema.sql:28-35  Customer   — покупатели
schema.sql:37-47  Purchase   — покупки, ссылается на Game и Customer
```

### Связи и каскадное удаление

```sql
-- schema.sql:24 — удалил жанр → удалятся все его игры
FOREIGN KEY (GenreID) REFERENCES Genre(GenreID) ON DELETE CASCADE

-- schema.sql:25 — удалил разработчика → удалятся его игры
FOREIGN KEY (DeveloperID) REFERENCES Developer(DeveloperID) ON DELETE CASCADE

-- schema.sql:46 — удалил игру → удалятся все её покупки
FOREIGN KEY (GameID) REFERENCES Game(GameID) ON DELETE CASCADE
```

**Цепочка:** удалил жанр → автоматом удалились игры → автоматом удалились покупки этих игр. База данных остаётся консистентной без дополнительного кода.

### JDBC URL

```
application.properties:1
jdbc:h2:mem:computer_games;DB_CLOSE_DELAY=-1
 │    │   │       │                └── не закрывать пока приложение работает
 │    │   │       └── имя базы (любое слово)
 │    │   └── mem = в памяти (данные исчезают при остановке)
 │    └── тип СУБД — H2
 └── протокол подключения

То же имя в GameService.java:12:
private static final String URL = "jdbc:h2:mem:computer_games";
```

---

## Как работает CRUD

[↑ Наверх](#навигация)

### CREATE (Добавление)

```
1. Кнопка «+ Добавить»               MainView.java:331
2. openAddGameDialog()                GameDialogManager.java:35
3. ComboBox жанра и разработчика      GameDialogManager.java:44-50
4. Нажал «Сохранить»
5. Валидация                          GameDialogManager.java:57
6. newID = max(ID) + 1               GameDialogManager.java:61
7. new Game(newID, title, ...)        GameDialogManager.java:62
8. gameService.createGame(game)       GameDialogManager.java:73
9. PreparedStatement INSERT           GameService.java:44-56
10. games.add(game)                   GameDialogManager.java:73
11. grid.setItems(games)              GameDialogManager.java:74
```

### READ (Чтение)

```
1. При запуске / переключении вкладки
2. gameService.getGames()             MainView.java:82 / 393
3. SELECT * FROM Game                 GameService.java:18
4. ResultSet → while rs.next()        GameService.java:24
5. new Game(rs.getInt(...), ...)      GameService.java:25-33
6. grid.setItems(games)               MainView.java:394
```

### UPDATE (Изменение)

```
1. Кнопка «Изменить»                  MainView.java:334
2. openEditGameDialog()               GameDialogManager.java:82
3. Форма предзаполнена текущими значениями (строки 84-100)
4. Нажал «Сохранить»
5. selected.setTitle(...)             GameDialogManager.java:125
6. gameService.updateGame(selected)   GameDialogManager.java:130
7. UPDATE Game SET ... WHERE GameID=? GameService.java:62-84
8. grid.getDataProvider().refreshAll() — таблица перерисована
```

### DELETE (Удаление)

```
1. Кнопка «Удалить»                   MainView.java:337
2. Диалог подтверждения               GameDialogManager.java:143
3. gameService.deleteGame(id)         GameDialogManager.java:151
4. DELETE FROM Game WHERE GameID=?    GameService.java:89-92
5. ON DELETE CASCADE                  schema.sql:46 — покупки удалятся автоматически
6. games.remove(selected)             GameDialogManager.java:152
7. grid.setItems(games)               GameDialogManager.java:153
```

---

## Фильтры и поиск

[↑ Наверх](#навигация)

### Многокритериальная фильтрация игр

```java
// MainView.java:119-138 — Runnable = интерфейс с методом run()
Runnable applyGameFilter = () -> {
    grid.setItems(
        games.stream()          // берём весь список
            .filter(g -> title.isEmpty() ||
                g.getTitle().toLowerCase().contains(title)) // поиск по подстроке
            .filter(g -> genre == null ||
                g.getGenreID() == genre.getGenreID())        // по жанру
            .filter(g -> minP == null || g.getPrice() >= minP) // цена от
            .filter(g -> maxP == null || g.getPrice() <= maxP) // цена до
            .collect(Collectors.toList())
    );
};
// каждый фильтр триггерит пересчёт:
gameTitleSearch.addValueChangeListener(e -> applyGameFilter.run()); // строка 136
```

**Ключевая идея:** если значение не задано (`null` / пустая строка) — фильтр пропускает всё. Все фильтры комбинируются через цепочку `.filter()`.

### FieldFilter — для справочных таблиц

```java
// FieldFilter.java:7
public class FieldFilter<T, V> {  // T = тип объекта, V = тип поля

// FieldFilter.java:15-18 — уникальные значения
this.availableValues = items.stream()
        .map(fieldExtractor)             // Game → title
        .filter(Objects::nonNull)        // убрать null
        .collect(Collectors.toSet());    // Set = нет дублей

// FieldFilter.java:28-33 — фильтрация
return items.stream()
        .filter(item -> Objects.equals(fieldExtractor.apply(item), value))
        .collect(Collectors.toList());
```

### Что такое Stream API

```
список → .stream() → конвейер операций → .collect() → новый список

Операции:
.filter(условие)       — оставить только те, где true
.map(функция)          — преобразовать каждый элемент
.mapToDouble(функция)  — преобразовать в поток чисел
.sum()                 — сумма всех чисел
.sorted(компаратор)    — отсортировать
.limit(n)              — взять первые n элементов
.findFirst()           — взять первый элемент
.collect(toList())     — собрать в List
.collect(toSet())      — собрать в Set (без дублей)
```

**Примеры из проекта:**
```java
// Сумма выручки (DashboardView.java:57):
completed.stream().mapToDouble(Purchase::getTotalAmount).sum()

// Автогенерация ID (GameDialogManager.java:61):
games.stream().mapToInt(Game::getGameID).max().orElse(0) + 1

// Найти жанр по ID (GameDialogManager.java:97):
genres.stream().filter(g -> g.getGenreID() == selected.getGenreID()).findFirst()
```

---

## Дашборд

[↑ Наверх](#навигация)

### Метрики разбиты по статусу

```java
// DashboardView.java:23-24
private static final Set<String> PAID = Set.of("Завершён", "Оплачено");

// DashboardView.java:51
List<Purchase> completed = purchases.stream()
    .filter(p -> p.getStatus() != null && PAID.contains(p.getStatus()))
    .collect(Collectors.toList());

// DashboardView.java:57 — выручка только по оплаченным
double revenue = completed.stream().mapToDouble(Purchase::getTotalAmount).sum();
```

Карточки: **Выручка** (только PAID) / **Завершено** / **В обработке** / **Отменено** / **Покупателей** / **Игр**.

### График по месяцам

```java
// DashboardView.java:147-162 — группируем по названию месяца
for (Purchase p : list) {
    String month = p.getPurchaseDate()
        .getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru")); // "январь"
    salesByMonth.put(month, salesByMonth.getOrDefault(month, 0.0) + p.getTotalAmount());
}
// Полосы рисуются через CSS width в процентах от максимума
```

### SVG diаграмма без библиотек

```java
// DashboardView.java:324-365
int cx=120, cy=120, r=100, ri=52; // центр, внешний/внутренний радиусы бублика

// для каждого жанра:
double sweep = pct * 360.0;   // угол сектора
// 4 угловые точки через тригонометрию
x = cx + radius * Math.cos(Math.toRadians(angle));
y = cy + radius * Math.sin(Math.toRadians(angle));
// SVG команды: M=move, A=arc, L=line, Z=close
"<path d='M... A... L... A... Z' fill='цвет'/>"

// строка 348 — % внутри сектора если >= 7%
if (pct >= 0.07) { добавить <text> по середине дуги }

// строка 362 — вставка SVG в Vaadin:
container.getElement().setProperty("innerHTML", svgString);
```

### Обновление при открытии вкладки

```java
// MainView.java:411-419
} else if (selectedTab == dashboardTab) {
    dashboardContent.removeAll();                 // строка 412 — убираем старый
    dashboardContent.add(new DashboardView(       // строка 413 — создаём новый
        purchaseService.getPurchases(), ...        // строка 414 — данные прямо из БД
    ));
}
```

---

## Топ-10 вопросов

[↑ Наверх](#навигация)

### В1. Что делает программа?

Веб-приложение для управления магазином компьютерных игр. Ведёт каталог игр, учитывает покупателей и покупки, управляет справочниками жанров и разработчиков, анализирует продажи через дашборд с разбивкой по статусам заказов.

[↑ К вопросам](#топ-10-вопросов)

---

### В2. Какие технологии?

- **Java 17** — язык
- **Spring Boot 3.2.5** — каркас: Tomcat, DI-контейнер (`@Service`), init базы
- **Vaadin 24** — веб-интерфейс на Java (`Grid`, `ComboBox`, `Dialog`, `Tabs`)
- **H2** — встроенная in-memory база, полный SQL
- **JDBC** — прямые запросы через `PreparedStatement` (без ORM/JPA)
- **Maven** — сборка (`pom.xml`)

[↑ К вопросам](#топ-10-вопросов)

---

### В3. Почему H2, а не MySQL/PostgreSQL?

H2 запускается прямо внутри приложения — не нужен отдельный сервер. Идеально для разработки и демонстрации. Чтобы переключиться на PostgreSQL — изменить только `application.properties:1` и добавить зависимость. Вся логика сервисов останется без изменений.

[↑ К вопросам](#топ-10-вопросов)

---

### В4. Как связаны таблицы?

Через `FOREIGN KEY` в `schema.sql`. `Game` ссылается на `Genre` (строка 24) и `Developer` (строка 25). `Purchase` ссылается на `Game` (строка 46) и `Customer` (строка 45). `ON DELETE CASCADE` обеспечивает автоматическое удаление дочерних записей.

[↑ К вопросам](#топ-10-вопросов)

---

### В5. Что такое Spring Boot?

Фреймворк для Java-приложений. Без него: вручную настраивать сервер, соединение с БД, создавать объекты зависимостей. С ним: `@SpringBootApplication` (строка 8 Application.java) — и всё само поднимается. `@Service` на классе (`GameService.java:10`) — Spring сам создаёт объект и передаёт в `MainView` через конструктор (Dependency Injection).

[↑ К вопросам](#топ-10-вопросов)

---

### В6. Что такое Vaadin?

Java-фреймворк для веб-интерфейса. Пишешь Java-код — получаешь страницу в браузере, без HTML и JS в исходниках. `Grid<Game>` — таблица, `ComboBox<Genre>` — выпадашка, `Dialog` — модальное окно, `Tabs` — вкладки. Vaadin компилирует их в браузерный JavaScript незаметно для разработчика.

[↑ К вопросам](#топ-10-вопросов)

---

### В7. Как реализован CRUD?

Для каждой сущности — `*Service` с 4 методами (`get`, `create`, `update`, `delete`) через JDBC `PreparedStatement`. `*DialogManager` открывает диалог, принимает данные из формы, вызывает нужный метод сервиса, обновляет `Grid.setItems()`. Например: `GameService.java:16-94`, `GameDialogManager.java:35-158`.

[↑ К вопросам](#топ-10-вопросов)

---

### В8. Почему данные не устаревают?

`MainView.java:386` — слушатель `addSelectedChangeListener`. При каждом переключении вкладки: `clear()` + `addAll(service.getXxx())` + `grid.setItems()`. Для дашборда — пересоздаётся весь `DashboardView` с новыми данными (строка 413). Таким образом, таблица всегда отражает актуальное состояние базы.

[↑ К вопросам](#топ-10-вопросов)

---

### В9. Как работает дашборд?

При открытии вкладки — создаётся новый `DashboardView` (`MainView.java:413`). Покупки делятся на 3 группы по статусу (`DashboardView.java:51-55`). Выручка — `stream().mapToDouble().sum()` только по завершённым (строка 57). Круговая диаграмма — чистый SVG через тригонометрию (строки 324-365), без библиотек. Процент рисуется внутри сектора только если он ≥ 7% (строка 348).

[↑ К вопросам](#топ-10-вопросов)

---

### В10. Что такое Stream API?

Конвейер обработки коллекции без изменения оригинала. Используется везде:

```java
// Выручка (DashboardView.java:57):
completed.stream().mapToDouble(Purchase::getTotalAmount).sum()

// Фильтр по цене (MainView.java:126-130):
games.stream()
    .filter(g -> g.getPrice() >= minP)
    .collect(Collectors.toList())

// Найти жанр (GameDialogManager.java:97):
genres.stream()
    .filter(g -> g.getGenreID() == selected.getGenreID())
    .findFirst().ifPresent(genreBox::setValue)

// Уникальные имена (FieldFilter.java:15-18):
items.stream().map(extractor).filter(Objects::nonNull).collect(Collectors.toSet())
```

[↑ К вопросам](#топ-10-вопросов)

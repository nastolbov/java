package org.triangle.base.ui.view;

import java.util.List;
import java.util.stream.Collectors;
import org.triangle.base.ui.view.Dialog.*;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import org.triangle.base.Class.Model.*;
import org.triangle.base.ui.Components.FieldFilter;
import org.triangle.base.ui.service.*;
import java.util.HashMap;
import java.util.Map;

@Route("")
public class MainView extends VerticalLayout {

    private Tabs tabs;
    private Map<Tab, VerticalLayout> tabContents;

    private Grid<Game> grid = new Grid<>(Game.class);
    private GameService gameService;

    private Grid<Purchase> grid2 = new Grid<>(Purchase.class);
    private PurchaseService purchaseService;

    private Grid<Customer> grid3 = new Grid<>(Customer.class);
    private CustomerService customerService;

    private Grid<Developer> grid4 = new Grid<>(Developer.class);
    private DeveloperService developerService;

    private Grid<Genre> grid5 = new Grid<>(Genre.class);
    private GenreService genreService;
    private DashboardView dashboard;

    public MainView(GameService gameService, PurchaseService purchaseService, CustomerService customerService, DeveloperService developerService, GenreService genreService) {
        this.gameService = gameService;
        this.purchaseService = purchaseService;
        this.customerService = customerService;
        this.developerService = developerService;
        this.genreService = genreService;

        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "#f0f2f5");

        // ===== ХЕДЕР =====
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("app-header");
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        H2 title = new H2("🎮 Магазин компьютерных игр");
        title.addClassName("app-title");
        Span subtitle = new Span("Система управления");
        subtitle.addClassName("app-subtitle");
        header.add(title, subtitle);
        add(header);

        tabContents = new HashMap<>();

        tabs = new Tabs();
        tabs.setWidthFull();
        add(tabs);

        VerticalLayout content1 = new VerticalLayout();
        VerticalLayout content2 = new VerticalLayout();
        VerticalLayout content3 = new VerticalLayout();
        VerticalLayout content4 = new VerticalLayout();
        VerticalLayout content5 = new VerticalLayout();
        VerticalLayout content6 = new VerticalLayout();

        // тянем данные из бд
        List<Game> games = gameService.getGames();
        List<Purchase> purchases = purchaseService.getPurchases();
        List<Customer> customers = customerService.getCustomers();
        List<Developer> developers = developerService.getDevelopers();
        List<Genre> genres = genreService.getGenres();

        // менеджеры диалогов для каждой вкладки
        GameDialogManager manager = new GameDialogManager(gameService, grid, genres, developers);
        PurchaseDialogManager manager2 = new PurchaseDialogManager(purchaseService, grid2, customers, games, purchases);
        CustomerDialogManager manager3 = new CustomerDialogManager(customerService, grid3);
        DeveloperDialogManager manager4 = new DeveloperDialogManager(developerService, grid4);
        GenreDialogManager manager5 = new GenreDialogManager(genreService, grid5);

        // === Фильтры — Игры: поиск + жанр + диапазон цен ===
        TextField gameTitleSearch = new TextField();
        gameTitleSearch.setPlaceholder("Поиск по названию...");
        gameTitleSearch.setClearButtonVisible(true);
        gameTitleSearch.setWidth("210px");

        ComboBox<Genre> gameGenreFilter = new ComboBox<>();
        gameGenreFilter.setItems(genres);
        gameGenreFilter.setItemLabelGenerator(Genre::getName);
        gameGenreFilter.setPlaceholder("Жанр");
        gameGenreFilter.setClearButtonVisible(true);
        gameGenreFilter.setWidth("155px");

        NumberField minPriceField = new NumberField();
        minPriceField.setPlaceholder("Цена от");
        minPriceField.setWidth("110px");
        minPriceField.setMin(0);

        NumberField maxPriceField = new NumberField();
        maxPriceField.setPlaceholder("до");
        maxPriceField.setWidth("95px");
        maxPriceField.setMin(0);

        Runnable applyGameFilter = () -> {
            String q = gameTitleSearch.getValue() != null ? gameTitleSearch.getValue().toLowerCase() : "";
            Genre genre = gameGenreFilter.getValue();
            Double minP = minPriceField.getValue();
            Double maxP = maxPriceField.getValue();
            grid.setItems(games.stream()
                .filter(g -> q.isEmpty() || g.getTitle().toLowerCase().contains(q))
                .filter(g -> genre == null || g.getGenreID() == genre.getGenreID())
                .filter(g -> minP == null || g.getPrice() >= minP)
                .filter(g -> maxP == null || g.getPrice() <= maxP)
                .collect(Collectors.toList()));
        };
        gameTitleSearch.addValueChangeListener(e -> applyGameFilter.run());
        gameGenreFilter.addValueChangeListener(e -> applyGameFilter.run());
        minPriceField.addValueChangeListener(e -> applyGameFilter.run());
        maxPriceField.addValueChangeListener(e -> applyGameFilter.run());

        // === Фильтры — Покупки: покупатель + статус + диапазон суммы ===
        ComboBox<Customer> purchaseCustomerFilter = new ComboBox<>();
        purchaseCustomerFilter.setItems(customers);
        purchaseCustomerFilter.setItemLabelGenerator(Customer::getName);
        purchaseCustomerFilter.setPlaceholder("Покупатель");
        purchaseCustomerFilter.setClearButtonVisible(true);
        purchaseCustomerFilter.setWidth("210px");

        ComboBox<String> purchaseStatusFilter = new ComboBox<>();
        purchaseStatusFilter.setItems("Завершён", "Оплачено", "В обработке", "Ожидание", "Отменено");
        purchaseStatusFilter.setPlaceholder("Статус");
        purchaseStatusFilter.setClearButtonVisible(true);
        purchaseStatusFilter.setWidth("155px");

        NumberField minAmountField = new NumberField();
        minAmountField.setPlaceholder("Сумма от");
        minAmountField.setWidth("115px");
        minAmountField.setMin(0);

        NumberField maxAmountField = new NumberField();
        maxAmountField.setPlaceholder("до");
        maxAmountField.setWidth("95px");
        maxAmountField.setMin(0);

        Runnable applyPurchaseFilter = () -> {
            Customer cust = purchaseCustomerFilter.getValue();
            String status = purchaseStatusFilter.getValue();
            Double minA = minAmountField.getValue();
            Double maxA = maxAmountField.getValue();
            grid2.setItems(purchases.stream()
                .filter(p -> cust == null || p.getCustomerID() == cust.getCustomerID())
                .filter(p -> status == null || status.equals(p.getStatus()))
                .filter(p -> minA == null || p.getTotalAmount() >= minA)
                .filter(p -> maxA == null || p.getTotalAmount() <= maxA)
                .collect(Collectors.toList()));
        };
        purchaseCustomerFilter.addValueChangeListener(e -> applyPurchaseFilter.run());
        purchaseStatusFilter.addValueChangeListener(e -> applyPurchaseFilter.run());
        minAmountField.addValueChangeListener(e -> applyPurchaseFilter.run());
        maxAmountField.addValueChangeListener(e -> applyPurchaseFilter.run());

        // === Фильтры — Покупатели, Разработчики, Жанры ===
        FieldFilter<Customer, String> customersFilter = new FieldFilter<>(customers, Customer::getName);
        Span customersFilterLabel = new Span("Покупатель");
        customersFilterLabel.addClassName("filter-label");
        ComboBox<String> customersComboBox = new ComboBox<>();
        customersComboBox.setItems(customersFilter.getAvailableValues());
        customersComboBox.setPlaceholder("Выберите покупателя");
        customersComboBox.addValueChangeListener(event -> {
            String sel = event.getValue();
            grid3.setItems(sel != null ? customersFilter.filterByValue(sel) : customers);
        });

        FieldFilter<Developer, String> developerFilter = new FieldFilter<>(developers, Developer::getName);
        Span developersFilterLabel = new Span("Разработчик");
        developersFilterLabel.addClassName("filter-label");
        ComboBox<String> developersComboBox = new ComboBox<>();
        developersComboBox.setItems(developerFilter.getAvailableValues());
        developersComboBox.setPlaceholder("Выберите разработчика");
        developersComboBox.addValueChangeListener(event -> {
            String sel = event.getValue();
            grid4.setItems(sel != null ? developerFilter.filterByValue(sel) : developers);
        });

        FieldFilter<Genre, String> genreFilter = new FieldFilter<>(genres, Genre::getName);
        Span genreFilterLabel = new Span("Жанр");
        genreFilterLabel.addClassName("filter-label");
        ComboBox<String> genreComboBox = new ComboBox<>();
        genreComboBox.setItems(genreFilter.getAvailableValues());
        genreComboBox.setPlaceholder("Выберите жанр");
        genreComboBox.addValueChangeListener(event -> {
            String sel = event.getValue();
            grid5.setItems(sel != null ? genreFilter.filterByValue(sel) : genres);
        });

        // заполняем таблицы
        grid.setItems(games);
        grid2.setItems(purchases);
        grid3.setItems(customers);
        grid4.setItems(developers);
        grid5.setItems(genres);

        // === Игры: жанр и разработчик — по имени, цена с символом ₽ ===
        grid.removeAllColumns();
        grid.addColumn(Game::getGameID).setHeader("Код").setWidth("70px").setFlexGrow(0).setResizable(true);
        grid.addColumn(Game::getTitle).setHeader("Название").setResizable(true);
        grid.addColumn(Game::getDescription).setHeader("Описание").setResizable(true);
        grid.addColumn(g -> String.format("%.2f ₽", g.getPrice())).setHeader("Цена").setWidth("120px").setFlexGrow(0).setResizable(true);
        grid.addColumn(Game::getStockQuantity).setHeader("На складе").setWidth("105px").setFlexGrow(0).setResizable(true);
        grid.addColumn(g -> genres.stream().filter(x -> x.getGenreID() == g.getGenreID()).map(Genre::getName).findFirst().orElse("—")).setHeader("Жанр").setResizable(true);
        grid.addColumn(g -> developers.stream().filter(x -> x.getDeveloperID() == g.getDeveloperID()).map(Developer::getName).findFirst().orElse("—")).setHeader("Разработчик").setResizable(true);

        // === Покупки: имя покупателя и название игры, цветной статус ===
        grid2.removeAllColumns();
        grid2.addColumn(Purchase::getPurchaseID).setHeader("Код").setWidth("70px").setFlexGrow(0).setResizable(true);
        grid2.addColumn(p -> customers.stream().filter(c -> c.getCustomerID() == p.getCustomerID()).map(Customer::getName).findFirst().orElse("—")).setHeader("Покупатель").setResizable(true);
        grid2.addColumn(p -> games.stream().filter(g -> g.getGameID() == p.getGameID()).map(Game::getTitle).findFirst().orElse("—")).setHeader("Игра").setResizable(true);
        grid2.addColumn(Purchase::getPurchaseDate).setHeader("Дата").setWidth("115px").setFlexGrow(0).setResizable(true);
        grid2.addColumn(p -> String.format("%.2f ₽", p.getTotalAmount())).setHeader("Сумма").setWidth("125px").setFlexGrow(0).setResizable(true);
        grid2.addColumn(new ComponentRenderer<>(p -> {
            Span badge = new Span(p.getStatus() != null ? p.getStatus() : "");
            badge.addClassName("status-badge");
            if (p.getStatus() != null) {
                switch (p.getStatus()) {
                    case "Завершён": case "Оплачено": badge.addClassName("status-success"); break;
                    case "В обработке": case "Ожидание": badge.addClassName("status-warning"); break;
                    case "Отменено": badge.addClassName("status-danger"); break;
                    default: badge.addClassName("status-neutral"); break;
                }
            }
            return badge;
        })).setHeader("Статус").setWidth("140px").setFlexGrow(0).setResizable(true);
        grid2.addColumn(Purchase::getCount).setHeader("Кол-во").setWidth("88px").setFlexGrow(0).setResizable(true);

        // === Покупатели ===
        grid3.setColumns("customerID", "name", "email", "phoneNumber", "address", "registrationDate");
        grid3.getColumnByKey("customerID").setHeader("Код").setWidth("70px").setFlexGrow(0);
        grid3.getColumnByKey("name").setHeader("ФИО");
        grid3.getColumnByKey("email").setHeader("Почта");
        grid3.getColumnByKey("phoneNumber").setHeader("Телефон");
        grid3.getColumnByKey("address").setHeader("Адрес");
        grid3.getColumnByKey("registrationDate").setHeader("Дата регистрации");
        grid3.getColumns().forEach(col -> col.setResizable(true));

        // === Разработчики ===
        grid4.setColumns("developerID", "name", "contactName", "phoneNumber", "email", "address");
        grid4.getColumnByKey("developerID").setHeader("Код").setWidth("70px").setFlexGrow(0);
        grid4.getColumnByKey("name").setHeader("Название");
        grid4.getColumnByKey("contactName").setHeader("Контактное лицо");
        grid4.getColumnByKey("phoneNumber").setHeader("Телефон");
        grid4.getColumnByKey("email").setHeader("Почта");
        grid4.getColumnByKey("address").setHeader("Адрес");
        grid4.getColumns().forEach(col -> col.setResizable(true));

        // === Жанры ===
        grid5.setColumns("genreID", "name", "description");
        grid5.getColumnByKey("genreID").setHeader("Код").setWidth("70px").setFlexGrow(0);
        grid5.getColumnByKey("name").setHeader("Название");
        grid5.getColumnByKey("description").setHeader("Описание");
        grid5.getColumns().forEach(col -> col.setResizable(true));

        // тулбары (фильтр + кнопки вместе)
        HorizontalLayout hvc1 = new HorizontalLayout();
        HorizontalLayout hvc2 = new HorizontalLayout();
        HorizontalLayout hvc3 = new HorizontalLayout();
        HorizontalLayout hvc4 = new HorizontalLayout();
        HorizontalLayout hvc5 = new HorizontalLayout();

        for (HorizontalLayout h : new HorizontalLayout[]{hvc1,hvc2,hvc3,hvc4,hvc5}) {
            h.addClassName("toolbar");
            h.setWidthFull();
            h.setAlignItems(Alignment.BASELINE);
        }

        hvc1.add(gameTitleSearch, gameGenreFilter, minPriceField, maxPriceField);
        hvc2.add(purchaseCustomerFilter, purchaseStatusFilter, minAmountField, maxAmountField);
        hvc3.add(customersFilterLabel, customersComboBox);
        hvc4.add(developersFilterLabel, developersComboBox);
        hvc5.add(genreFilterLabel, genreComboBox);

        // таблицы — высота на весь экран минус хедер/табы/тулбар
        String gridH = "calc(100vh - 220px)";
        for (Grid<?> g : new Grid[]{grid, grid2, grid3, grid4, grid5}) {
            g.setHeight(gridH);
            g.setWidthFull();
        }

        // оборачиваем таблицы в карточки
        Div gridCard1 = new Div(grid);  gridCard1.addClassName("grid-card"); gridCard1.setWidthFull();
        Div gridCard2 = new Div(grid2); gridCard2.addClassName("grid-card"); gridCard2.setWidthFull();
        Div gridCard3 = new Div(grid3); gridCard3.addClassName("grid-card"); gridCard3.setWidthFull();
        Div gridCard4 = new Div(grid4); gridCard4.addClassName("grid-card"); gridCard4.setWidthFull();
        Div gridCard5 = new Div(grid5); gridCard5.addClassName("grid-card"); gridCard5.setWidthFull();

        content1.addClassName("main-content"); content1.add(hvc1, gridCard1);
        content2.addClassName("main-content"); content2.add(hvc2, gridCard2);
        content3.addClassName("main-content"); content3.add(hvc3, gridCard3);
        content4.addClassName("main-content"); content4.add(hvc4, gridCard4);
        content5.addClassName("main-content"); content5.add(hvc5, gridCard5);

        // кнопки для Игр
        Button addButton = styledBtn("+ Добавить", "btn-add");
        addButton.addClickListener(e -> manager.openAddGameDialog(games));
        Button chButton = styledBtn("Изменить", "btn-edit");
        chButton.addClickListener(e -> manager.openEditGameDialog());
        Button delButton = styledBtn("Удалить", "btn-delete");
        delButton.addClickListener(e -> manager.openDelGameDialog(games));
        hvc1.add(addButton, chButton, delButton);

        // кнопки для Покупок
        Button addButton2 = styledBtn("+ Добавить", "btn-add");
        addButton2.addClickListener(e -> manager2.openAddPurchaseDialog(purchases));
        Button chButton2 = styledBtn("Изменить", "btn-edit");
        chButton2.addClickListener(e -> manager2.openEditPurchaseDialog());
        Button delButton2 = styledBtn("Удалить", "btn-delete");
        delButton2.addClickListener(e -> manager2.openDelPurchaseDialog(purchases));
        hvc2.add(addButton2, chButton2, delButton2);

        // кнопки для Покупателей
        Button addButton3 = styledBtn("+ Добавить", "btn-add");
        addButton3.addClickListener(e -> manager3.openAddCustomerDialog(customers));
        Button chButton3 = styledBtn("Изменить", "btn-edit");
        chButton3.addClickListener(e -> manager3.openEditCustomerDialog());
        Button delButton3 = styledBtn("Удалить", "btn-delete");
        delButton3.addClickListener(e -> manager3.openDelCustomerDialog());
        hvc3.add(addButton3, chButton3, delButton3);

        // кнопки для Разработчиков
        Button addButton4 = styledBtn("+ Добавить", "btn-add");
        addButton4.addClickListener(e -> manager4.openAddDeveloperDialog(developers));
        Button chButton4 = styledBtn("Изменить", "btn-edit");
        chButton4.addClickListener(e -> manager4.openEditDeveloperDialog());
        Button delButton4 = styledBtn("Удалить", "btn-delete");
        delButton4.addClickListener(e -> manager4.openDelDeveloperDialog(developers));
        hvc4.add(addButton4, chButton4, delButton4);

        // кнопки для Жанров
        Button addButton5 = styledBtn("+ Добавить", "btn-add");
        addButton5.addClickListener(e -> manager5.openAddGenreDialog(genres));
        Button chButton5 = styledBtn("Изменить", "btn-edit");
        chButton5.addClickListener(e -> manager5.openEditGenreDialog());
        Button delButton5 = styledBtn("Удалить", "btn-delete");
        delButton5.addClickListener(e -> manager5.openDelGenreDialog());
        hvc5.add(addButton5, chButton5, delButton5);

        Tab tab1 = new Tab("Игры");
        Tab tab2 = new Tab("Покупки");
        Tab tab3 = new Tab("Покупатели");
        Tab tab4 = new Tab("Разработчики");
        Tab tab5 = new Tab("Жанры");
        Tab tab6 = new Tab("Дашборд");

        dashboard = new DashboardView(purchases, genres, games, customers, developers);
        content6.add(dashboard);
        tabContents.put(tab1, content1);
        tabContents.put(tab2, content2);
        tabContents.put(tab3, content3);
        tabContents.put(tab4, content4);
        tabContents.put(tab5, content5);
        tabContents.put(tab6, content6);
        tabs.add(tab1, tab2, tab3, tab4, tab5, tab6);

        VerticalLayout mainContent = new VerticalLayout();
        add(mainContent);

        // переключение вкладок
        Tab dashboardTab = tab6;
        VerticalLayout dashboardContent = content6;
        tabs.addSelectedChangeListener(event -> {
            mainContent.removeAll();
            Tab selectedTab = event.getSelectedTab();

            // перечитываем данные из БД при каждом переключении вкладки
            if (selectedTab == tab1) {
                games.clear();
                games.addAll(gameService.getGames());
                grid.setItems(games);
            } else if (selectedTab == tab2) {
                purchases.clear();
                purchases.addAll(purchaseService.getPurchases());
                grid2.setItems(purchases);
            } else if (selectedTab == tab3) {
                customers.clear();
                customers.addAll(customerService.getCustomers());
                grid3.setItems(customers);
            } else if (selectedTab == tab4) {
                developers.clear();
                developers.addAll(developerService.getDevelopers());
                grid4.setItems(developers);
            } else if (selectedTab == tab5) {
                genres.clear();
                genres.addAll(genreService.getGenres());
                grid5.setItems(genres);
            } else if (selectedTab == dashboardTab) {
                dashboardContent.removeAll();
                dashboardContent.add(new DashboardView(
                        purchaseService.getPurchases(),
                        genreService.getGenres(),
                        gameService.getGames(),
                        customerService.getCustomers(),
                        developerService.getDevelopers()
                ));
            }

            VerticalLayout content = tabContents.get(selectedTab);
            if (content != null) {
                mainContent.add(content);
            }
        });

        // по умолчанию показываем первую вкладку
        Tab initialTab = tabs.getSelectedTab();
        if (initialTab != null) {
            mainContent.add(tabContents.get(initialTab));
        }
    }

    private Button styledBtn(String label, String cssClass) {
        Button btn = new Button(label);
        btn.addClassName(cssClass);
        return btn;
    }
}

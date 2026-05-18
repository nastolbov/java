package org.triangle.base.ui.view;

import java.util.List;
import org.triangle.base.ui.view.Dialog.*;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
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
        PurchaseDialogManager manager2 = new PurchaseDialogManager(purchaseService, grid2, customers, games);
        CustomerDialogManager manager3 = new CustomerDialogManager(customerService, grid3);
        DeveloperDialogManager manager4 = new DeveloperDialogManager(developerService, grid4);
        GenreDialogManager manager5 = new GenreDialogManager(genreService, grid5);

        // фильтры для каждой таблицы
        FieldFilter<Game, String> gamesFilter = new FieldFilter<>(games, Game::getTitle);
        FieldFilter<Purchase, Integer> purchasesFilter = new FieldFilter<>(purchases, Purchase::getPurchaseID);
        FieldFilter<Customer, String> customersFilter = new FieldFilter<>(customers, Customer::getName);
        FieldFilter<Developer, String> developerFilter = new FieldFilter<>(developers, Developer::getName);
        FieldFilter<Genre, String> genreFilter = new FieldFilter<>(genres, Genre::getName);

        List<String> gameNames = gamesFilter.getAvailableValues();
        List<Integer> purchaseIds = purchasesFilter.getAvailableValues();
        List<String> customerNames = customersFilter.getAvailableValues();
        List<String> developerNames = developerFilter.getAvailableValues();
        List<String> genreNames = genreFilter.getAvailableValues();

        // комбобоксы для фильтрации
        Span gameFilterLabel = new Span("Игра");
        ComboBox<String> gameComboBox = new ComboBox<>();
        gameComboBox.setItems(gameNames);
        gameComboBox.setPlaceholder("Выберите игру");
        gameComboBox.addValueChangeListener(event -> {
            String selected = event.getValue();
            if (selected != null) {
                grid.setItems(gamesFilter.filterByValue(selected));
            } else {
                grid.setItems(games);
            }
        });

        Span purchasesFilterLabel = new Span("Номер покупки");
        ComboBox<Integer> purchasesComboBox = new ComboBox<>();
        purchasesComboBox.setItems(purchaseIds);
        purchasesComboBox.setPlaceholder("Выберите покупку");
        purchasesComboBox.addValueChangeListener(event -> {
            Integer selected = event.getValue();
            if (selected != null) {
                grid2.setItems(purchasesFilter.filterByValue(selected));
            } else {
                grid2.setItems(purchases);
            }
        });

        Span customersFilterLabel = new Span("Покупатель");
        ComboBox<String> customersComboBox = new ComboBox<>();
        customersComboBox.setItems(customerNames);
        customersComboBox.setPlaceholder("Выберите покупателя");
        customersComboBox.addValueChangeListener(event -> {
            String selected = event.getValue();
            if (selected != null) {
                grid3.setItems(customersFilter.filterByValue(selected));
            } else {
                grid3.setItems(customers);
            }
        });

        Span developersFilterLabel = new Span("Разработчик");
        ComboBox<String> developersComboBox = new ComboBox<>();
        developersComboBox.setItems(developerNames);
        developersComboBox.setPlaceholder("Выберите разработчика");
        developersComboBox.addValueChangeListener(event -> {
            String selected = event.getValue();
            if (selected != null) {
                grid4.setItems(developerFilter.filterByValue(selected));
            } else {
                grid4.setItems(developers);
            }
        });

        Span genreFilterLabel = new Span("Жанр");
        ComboBox<String> genreComboBox = new ComboBox<>();
        genreComboBox.setItems(genreNames);
        genreComboBox.setPlaceholder("Выберите жанр");
        genreComboBox.addValueChangeListener(event -> {
            String selected = event.getValue();
            if (selected != null) {
                grid5.setItems(genreFilter.filterByValue(selected));
            } else {
                grid5.setItems(genres);
            }
        });

        // заполняем таблицы
        grid.setItems(games);
        grid2.setItems(purchases);
        grid3.setItems(customers);
        grid4.setItems(developers);
        grid5.setItems(genres);

        // порядок столбцов — ID первым
        grid.setColumns("gameID", "title", "description", "price", "stockQuantity", "genreID", "developerID");
        grid2.setColumns("purchaseID", "customerID", "gameID", "purchaseDate", "totalAmount", "status", "count");
        grid3.setColumns("customerID", "name", "email", "phoneNumber", "address", "registrationDate");
        grid4.setColumns("developerID", "name", "contactName", "phoneNumber", "email", "address");
        grid5.setColumns("genreID", "name", "description");

        // русские заголовки — Игры
        grid.getColumnByKey("gameID").setHeader("Код игры");
        grid.getColumnByKey("title").setHeader("Название");
        grid.getColumnByKey("description").setHeader("Описание");
        grid.getColumnByKey("price").setHeader("Цена");
        grid.getColumnByKey("stockQuantity").setHeader("Количество");
        grid.getColumnByKey("genreID").setHeader("Код жанра");
        grid.getColumnByKey("developerID").setHeader("Код разработчика");
        // Покупки
        grid2.getColumnByKey("purchaseID").setHeader("Код покупки");
        grid2.getColumnByKey("customerID").setHeader("Код покупателя");
        grid2.getColumnByKey("gameID").setHeader("Код игры");
        grid2.getColumnByKey("purchaseDate").setHeader("Дата покупки");
        grid2.getColumnByKey("totalAmount").setHeader("Итоговая сумма");
        grid2.getColumnByKey("status").setHeader("Статус покупки");
        grid2.getColumnByKey("count").setHeader("Количество");
        // Покупатели
        grid3.getColumnByKey("customerID").setHeader("Код покупателя");
        grid3.getColumnByKey("name").setHeader("ФИО");
        grid3.getColumnByKey("email").setHeader("Почта");
        grid3.getColumnByKey("phoneNumber").setHeader("Номер телефона");
        grid3.getColumnByKey("address").setHeader("Адрес");
        grid3.getColumnByKey("registrationDate").setHeader("Дата регистрации");
        // Разработчики
        grid4.getColumnByKey("developerID").setHeader("Код разработчика");
        grid4.getColumnByKey("name").setHeader("Название");
        grid4.getColumnByKey("contactName").setHeader("Контактное имя");
        grid4.getColumnByKey("phoneNumber").setHeader("Номер телефона");
        grid4.getColumnByKey("email").setHeader("Почта");
        grid4.getColumnByKey("address").setHeader("Адрес");
        // Жанры
        grid5.getColumnByKey("genreID").setHeader("Код жанра");
        grid5.getColumnByKey("name").setHeader("Название");
        grid5.getColumnByKey("description").setHeader("Описание");

        // растягиваемые столбцы
        grid.getColumns().forEach(col -> col.setResizable(true));
        grid2.getColumns().forEach(col -> col.setResizable(true));
        grid3.getColumns().forEach(col -> col.setResizable(true));
        grid4.getColumns().forEach(col -> col.setResizable(true));
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

        gameFilterLabel.addClassName("filter-label");
        purchasesFilterLabel.addClassName("filter-label");
        customersFilterLabel.addClassName("filter-label");
        developersFilterLabel.addClassName("filter-label");
        genreFilterLabel.addClassName("filter-label");

        hvc1.add(gameFilterLabel, gameComboBox);
        hvc2.add(purchasesFilterLabel, purchasesComboBox);
        hvc3.add(customersFilterLabel, customersComboBox);
        hvc4.add(developersFilterLabel, developersComboBox);
        hvc5.add(genreFilterLabel, genreComboBox);

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

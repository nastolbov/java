package org.triangle.base.ui.view;

import java.util.List;
import org.triangle.base.ui.view.Dialog.*;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
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

@Route("") // стартовая страница
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

        // менеджеры диалогов для каждой вкладки
        GameDialogManager manager = new GameDialogManager(gameService, grid);
        PurchaseDialogManager manager2 = new PurchaseDialogManager(purchaseService, grid2);
        CustomerDialogManager manager3 = new CustomerDialogManager(customerService, grid3);
        DeveloperDialogManager manager4 = new DeveloperDialogManager(developerService, grid4);
        GenreDialogManager manager5 = new GenreDialogManager(genreService, grid5);
        tabContents = new HashMap<>();

        tabs = new Tabs();
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

        // чтоб столбцы можно было растягивать
        grid.getColumns().forEach(col -> col.setResizable(true));
        grid2.getColumns().forEach(col -> col.setResizable(true));
        grid3.getColumns().forEach(col -> col.setResizable(true));
        grid4.getColumns().forEach(col -> col.setResizable(true));
        grid5.getColumns().forEach(col -> col.setResizable(true));

        // русские названия столбцов - Игры
        grid.getColumns().forEach(column -> {
            String key = column.getKey();
            switch (key) {
                case "title": column.setHeader("Название"); break;
                case "price": column.setHeader("Цена"); break;
                case "description": column.setHeader("Описание"); break;
                case "stockQuantity": column.setHeader("Количество"); break;
                case "gameID": column.setHeader("Код игры"); break;
                case "genreID": column.setHeader("Код жанра"); break;
                case "developerID": column.setHeader("Код разработчика"); break;
            }
        });
        // Покупки
        grid2.getColumns().forEach(column -> {
            String key = column.getKey();
            switch (key) {
                case "count": column.setHeader("Количество"); break;
                case "customerID": column.setHeader("Код покупателя"); break;
                case "purchaseID": column.setHeader("Код покупки"); break;
                case "gameID": column.setHeader("Код игры"); break;
                case "status": column.setHeader("Статус покупки"); break;
                case "totalAmount": column.setHeader("Итоговая сумма"); break;
                case "purchaseDate": column.setHeader("Дата покупки"); break;
            }
        });
        // Покупатели
        grid3.getColumns().forEach(column -> {
            String key = column.getKey();
            switch (key) {
                case "address": column.setHeader("Адрес"); break;
                case "customerID": column.setHeader("Код покупателя"); break;
                case "email": column.setHeader("Почта"); break;
                case "name": column.setHeader("ФИО"); break;
                case "phoneNumber": column.setHeader("Номер телефона"); break;
                case "registrationDate": column.setHeader("Дата регистрации"); break;
            }
        });
        // Разработчики
        grid4.getColumns().forEach(column -> {
            String key = column.getKey();
            switch (key) {
                case "address": column.setHeader("Адрес"); break;
                case "developerID": column.setHeader("Код разработчика"); break;
                case "email": column.setHeader("Почта"); break;
                case "name": column.setHeader("Название"); break;
                case "phoneNumber": column.setHeader("Номер телефона"); break;
                case "contactName": column.setHeader("Контактное имя"); break;
            }
        });
        // Жанры
        grid5.getColumns().forEach(column -> {
            String key = column.getKey();
            switch (key) {
                case "description": column.setHeader("Описание"); break;
                case "genreID": column.setHeader("Код жанра"); break;
                case "name": column.setHeader("Название"); break;
            }
        });

        // контейнеры для кнопок
        HorizontalLayout hvc1 = new HorizontalLayout();
        HorizontalLayout hvc2 = new HorizontalLayout();
        HorizontalLayout hvc3 = new HorizontalLayout();
        HorizontalLayout hvc4 = new HorizontalLayout();
        HorizontalLayout hvc5 = new HorizontalLayout();

        // контейнеры для фильтров
        VerticalLayout filterLayout1 = new VerticalLayout();
        VerticalLayout filterLayout2 = new VerticalLayout();
        VerticalLayout filterLayout3 = new VerticalLayout();
        VerticalLayout filterLayout4 = new VerticalLayout();
        VerticalLayout filterLayout5 = new VerticalLayout();
        filterLayout1.add(gameFilterLabel, gameComboBox);
        filterLayout2.add(purchasesFilterLabel, purchasesComboBox);
        filterLayout3.add(customersFilterLabel, customersComboBox);
        filterLayout4.add(developersFilterLabel, developersComboBox);
        filterLayout5.add(genreFilterLabel, genreComboBox);

        content1.add(filterLayout1, grid);
        content2.add(filterLayout2, grid2);
        content3.add(filterLayout3, grid3);
        content4.add(filterLayout4, grid4);
        content5.add(filterLayout5, grid5);

        // кнопки для Игр
        Button addButton = new Button("Добавить");
        addButton.addClickListener(e -> manager.openAddGameDialog(games));
        Button chButton = new Button("Изменить");
        chButton.addClickListener(e -> manager.openEditGameDialog());
        Button delButton = new Button("Удалить");
        delButton.addClickListener(e -> manager.openDelGameDialog(games));
        hvc1.add(addButton, chButton, delButton);
        content1.add(hvc1);

        // кнопки для Покупок
        Button addButton2 = new Button("Добавить");
        addButton2.addClickListener(e -> manager2.openAddPurchaseDialog(purchases));
        Button chButton2 = new Button("Изменить");
        chButton2.addClickListener(e -> manager2.openEditPurchaseDialog());
        Button delButton2 = new Button("Удалить");
        delButton2.addClickListener(e -> manager2.openDelPurchaseDialog(purchases));
        hvc2.add(addButton2, chButton2, delButton2);
        content2.add(hvc2);

        // кнопки для Покупателей
        Button addButton3 = new Button("Добавить");
        addButton3.addClickListener(e -> manager3.openAddCustomerDialog(customers));
        Button chButton3 = new Button("Изменить");
        chButton3.addClickListener(e -> manager3.openEditCustomerDialog());
        Button delButton3 = new Button("Удалить");
        delButton3.addClickListener(e -> manager3.openDelCustomerDialog());
        hvc3.add(addButton3, chButton3, delButton3);
        content3.add(hvc3);

        // кнопки для Разработчиков
        Button addButton4 = new Button("Добавить");
        addButton4.addClickListener(e -> manager4.openAddDeveloperDialog(developers));
        Button chButton4 = new Button("Изменить");
        chButton4.addClickListener(e -> manager4.openEditDeveloperDialog());
        Button delButton4 = new Button("Удалить");
        delButton4.addClickListener(e -> manager4.openDelDeveloperDialog(developers));
        hvc4.add(addButton4, chButton4, delButton4);
        content4.add(hvc4);

        // кнопки для Жанров
        Button addButton5 = new Button("Добавить");
        addButton5.addClickListener(e -> manager5.openAddGenreDialog(genres));
        Button chButton5 = new Button("Изменить");
        chButton5.addClickListener(e -> manager5.openEditGenreDialog());
        Button delButton5 = new Button("Удалить");
        delButton5.addClickListener(e -> manager5.openDelGenreDialog());
        hvc5.add(addButton5, chButton5, delButton5);
        content5.add(hvc5);

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
        tabs.addSelectedChangeListener(event -> {
            mainContent.removeAll();
            Tab selectedTab = event.getSelectedTab();
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
}

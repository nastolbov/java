package org.triangle.base.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.grid.Grid;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

import org.triangle.base.Class.Model.*;

public class DashboardView extends VerticalLayout {

    private List<Game> games;
    private List<Purchase> purchases;
    private List<Customer> customers;
    private List<Developer> developers;
    private List<Genre> genres;

    public DashboardView(List<Purchase> purchases, List<Genre> genres, List<Game> games, List<Customer> customers, List<Developer> developers) {
        this.games = games;
        this.customers = customers;
        this.purchases = purchases;
        this.developers = developers;
        this.genres = genres;
        add(createDashboard());
    }

    private Component createDashboard() {
        VerticalLayout dashboardLayout = new VerticalLayout();

        // считаем общую сумму
        Double totalAmount = 0.0;
        for (Purchase purchase : purchases) {
            totalAmount += purchase.getTotalAmount();
        }

        // карточки сверху
        HorizontalLayout highlights = new HorizontalLayout();
        highlights.add(createHighlightCard("Общий доход", String.format("%.2f", totalAmount)));
        highlights.add(createHighlightCard("Количество покупок", String.valueOf(purchases.size())));
        highlights.add(createHighlightCard("Всего покупателей", String.valueOf(customers.size())));
        highlights.add(createHighlightCard("Всего игр", String.valueOf(games.size())));
        dashboardLayout.add(highlights);

        // собираем продажи по месяцам
        Map<String, Double> salesByMonth = new LinkedHashMap<>();
        List<LocalDate> purchaseDates = new ArrayList<>();
        for (Purchase purchase : purchases) {
            purchaseDates.add(purchase.getPurchaseDate());
        }
        purchaseDates.sort(Comparator.naturalOrder());

        Set<String> monthsSet = new LinkedHashSet<>();
        for (LocalDate date : purchaseDates) {
            String monthName = date.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru"));
            monthsSet.add(monthName);
        }
        for (String month : monthsSet) {
            salesByMonth.put(month, 0.0);
        }
        for (Purchase purchase : purchases) {
            String month = purchase.getPurchaseDate().getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru"));
            salesByMonth.put(month, salesByMonth.getOrDefault(month, 0.0) + purchase.getTotalAmount());
        }

        H3 salesTitle = new H3("Продажи по месяцам");
        Grid<Map.Entry<String, Double>> salesGrid = new Grid<>();
        salesGrid.addColumn(Map.Entry::getKey).setHeader("Месяц");
        salesGrid.addColumn(entry -> String.format("%.2f", entry.getValue())).setHeader("Сумма продаж");
        salesGrid.setItems(salesByMonth.entrySet());
        salesGrid.setHeight("250px");
        salesGrid.getColumns().forEach(col -> col.setResizable(true));

        // горизонтальные полоски - типа диаграмма
        VerticalLayout barChartLayout = new VerticalLayout();
        barChartLayout.getStyle().set("padding", "10px");
        double maxSale = salesByMonth.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
        for (Map.Entry<String, Double> entry : salesByMonth.entrySet()) {
            HorizontalLayout barRow = new HorizontalLayout();
            barRow.setAlignItems(Alignment.CENTER);
            barRow.setWidthFull();
            Span label = new Span(entry.getKey());
            label.setWidth("120px");
            int barWidth = (int) (entry.getValue() / maxSale * 400);
            Div bar = new Div();
            bar.getStyle()
                    .set("background-color", "#1976D2")
                    .set("height", "24px")
                    .set("width", barWidth + "px")
                    .set("border-radius", "4px");
            Span value = new Span(String.format("%.0f", entry.getValue()));
            value.getStyle().set("margin-left", "8px");
            barRow.add(label, bar, value);
            barChartLayout.add(barRow);
        }

        dashboardLayout.add(salesTitle, barChartLayout, salesGrid);

        // распределение по жанрам
        Map<Integer, Integer> gameToGenreMap = new HashMap<>();
        for (Game game : games) {
            gameToGenreMap.put(game.getGameID(), game.getGenreID());
        }
        Map<Integer, String> genreIdToName = new HashMap<>();
        for (Genre genre : genres) {
            genreIdToName.put(genre.getGenreID(), genre.getName());
        }

        Map<String, Integer> genreCounts = new HashMap<>();
        for (Purchase purchase : purchases) {
            Integer gameId = purchase.getGameID();
            Integer genreId = gameToGenreMap.get(gameId);
            String genreName = genreIdToName.getOrDefault(genreId, "Неизвестно");
            genreCounts.put(genreName, genreCounts.getOrDefault(genreName, 0) + 1);
        }

        H3 genreTitle = new H3("Распределение покупок по жанрам");

        // полоски по жанрам
        VerticalLayout pieLayout = new VerticalLayout();
        pieLayout.getStyle().set("padding", "10px");
        int totalOrders = purchases.size();
        for (Map.Entry<String, Integer> entry : genreCounts.entrySet()) {
            String genreName = entry.getKey();
            int count = entry.getValue();
            double percent = totalOrders > 0 ? (double) count / totalOrders * 100 : 0;
            int barWidth = (int) (percent * 4);

            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(Alignment.CENTER);
            row.setWidthFull();

            Span nameLabel = new Span(genreName);
            nameLabel.setWidth("150px");

            Div bar = new Div();
            bar.getStyle()
                    .set("background-color", getColor(genreCounts.keySet().stream().toList().indexOf(genreName)))
                    .set("height", "24px")
                    .set("width", barWidth + "px")
                    .set("border-radius", "4px");

            Span info = new Span(String.format("%d шт (%.1f%%)", count, percent));
            info.getStyle().set("margin-left", "8px");

            row.add(nameLabel, bar, info);
            pieLayout.add(row);
        }

        Grid<Map.Entry<String, Integer>> genreGrid = new Grid<>();
        genreGrid.addColumn(Map.Entry::getKey).setHeader("Жанр");
        genreGrid.addColumn(Map.Entry::getValue).setHeader("Количество покупок");
        genreGrid.addColumn(entry -> {
            double percent = totalOrders > 0 ? (double) entry.getValue() / totalOrders * 100 : 0;
            return String.format("%.1f%%", percent);
        }).setHeader("Доля (%)");
        genreGrid.setItems(genreCounts.entrySet());
        genreGrid.setHeight("250px");
        genreGrid.getColumns().forEach(col -> col.setResizable(true));

        dashboardLayout.add(genreTitle, pieLayout, genreGrid);
        return dashboardLayout;
    }

    private String getColor(int index) {
        String[] colors = {"#1976D2", "#388E3C", "#F57C00", "#D32F2F", "#7B1FA2", "#00796B", "#C2185B", "#512DA8"};
        return colors[index % colors.length];
    }

    public void redraw() {
        removeAll();
        add(createDashboard());
    }

    private Component createHighlightCard(String title, String value) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("200px");
        card.setHeight("100px");
        card.setJustifyContentMode(JustifyContentMode.CENTER);
        card.setAlignItems(Alignment.CENTER);
        card.getStyle().set("border", "1px solid #ccc")
                .set("border-radius", "8px")
                .set("padding", "10px");
        Span titleLabel = new Span(title);
        titleLabel.getStyle().set("font-weight", "bold");
        Span valueLabel = new Span(value);
        valueLabel.getStyle().set("font-size", "24px").set("color", "#2E7D32");
        card.add(titleLabel, valueLabel);
        return card;
    }
}

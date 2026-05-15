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
import java.util.Locale;

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

        int totalOrders = purchases.size();

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

        dashboardLayout.add(genreTitle, createPieChart(genreCounts, totalOrders), genreGrid);
        return dashboardLayout;
    }

    private Component createPieChart(Map<String, Integer> genreCounts, int total) {
        if (total == 0) return new Div();

        StringBuilder paths = new StringBuilder();
        StringBuilder legend = new StringBuilder();

        double startAngle = -90.0;
        int colorIndex = 0;
        int cx = 150, cy = 150, r = 130;

        for (Map.Entry<String, Integer> entry : genreCounts.entrySet()) {
            double percent = (double) entry.getValue() / total;
            double sweep = percent * 360.0;
            double endAngle = startAngle + sweep;

            double x1 = cx + r * Math.cos(Math.toRadians(startAngle));
            double y1 = cy + r * Math.sin(Math.toRadians(startAngle));
            double x2 = cx + r * Math.cos(Math.toRadians(endAngle));
            double y2 = cy + r * Math.sin(Math.toRadians(endAngle));

            int largeArc = sweep > 180 ? 1 : 0;
            String color = getColor(colorIndex);

            paths.append(String.format(Locale.US,
                "<path d='M %d %d L %.4f %.4f A %d %d 0 %d 1 %.4f %.4f Z' fill='%s' stroke='white' stroke-width='2'/>",
                cx, cy, x1, y1, r, r, largeArc, x2, y2, color));

            legend.append(String.format(
                "<div style='display:flex;align-items:center;margin:6px 0'>" +
                "<div style='width:16px;height:16px;min-width:16px;background:%s;border-radius:3px;margin-right:8px'></div>" +
                "<span style='font-size:14px'>%s — %d шт. (%.1f%%)</span></div>",
                color, entry.getKey(), entry.getValue(), percent * 100));

            startAngle = endAngle;
            colorIndex++;
        }

        String html = String.format(
            "<div style='display:flex;align-items:center;gap:32px;flex-wrap:wrap;padding:10px'>" +
            "<svg width='300' height='300' viewBox='0 0 300 300'>%s</svg>" +
            "<div>%s</div></div>",
            paths, legend);

        Div container = new Div();
        container.getElement().setProperty("innerHTML", html);
        return container;
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

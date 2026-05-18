package org.triangle.base.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;

import org.triangle.base.Class.Model.*;

public class DashboardView extends VerticalLayout {

    private List<Game> games;
    private List<Purchase> purchases;
    private List<Customer> customers;
    private List<Developer> developers;
    private List<Genre> genres;

    private static final Set<String> PAID = Set.of("Завершён", "Оплачено");
    private static final Set<String> PENDING = Set.of("В обработке", "Ожидание");

    public DashboardView(List<Purchase> purchases, List<Genre> genres, List<Game> games,
                         List<Customer> customers, List<Developer> developers) {
        this.games = games;
        this.customers = customers;
        this.purchases = purchases;
        this.developers = developers;
        this.genres = genres;
        setPadding(false);
        setSpacing(false);
        add(createDashboard());
    }

    private Component createDashboard() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("main-content");
        layout.setPadding(false);
        layout.setSpacing(false);

        // ── разбивка по статусу ──────────────────────────────────────
        List<Purchase> completed = purchases.stream()
                .filter(p -> p.getStatus() != null && PAID.contains(p.getStatus()))
                .collect(Collectors.toList());
        List<Purchase> pending = purchases.stream()
                .filter(p -> p.getStatus() != null && PENDING.contains(p.getStatus()))
                .collect(Collectors.toList());
        List<Purchase> cancelled = purchases.stream()
                .filter(p -> "Отменено".equals(p.getStatus()))
                .collect(Collectors.toList());

        double revenue = completed.stream().mapToDouble(Purchase::getTotalAmount).sum();
        double pendingAmt = pending.stream().mapToDouble(Purchase::getTotalAmount).sum();

        // ── карточки ─────────────────────────────────────────────────
        HorizontalLayout cards = new HorizontalLayout();
        cards.addClassName("dashboard-cards");
        cards.setWidthFull();
        cards.getStyle().set("padding", "20px 0 16px 0").set("gap", "16px").set("flex-wrap", "wrap");

        cards.add(
            card("Выручка",           String.format("%.0f ₽", revenue),          "card-success",  "Только оплаченные"),
            card("Завершено покупок",  String.valueOf(completed.size()),            "card-primary",  "Оплачено / Завершён"),
            card("В обработке",        String.valueOf(pending.size()),              "card-warning",  String.format("%.0f ₽ ожидает", pendingAmt)),
            card("Отменено",           String.valueOf(cancelled.size()),            "card-danger",   "Отменённые заказы"),
            card("Покупателей",        String.valueOf(customers.size()),            "card-neutral",  "Всего в базе"),
            card("Игр в каталоге",    String.valueOf(games.size()),                "card-neutral",  "Всего позиций")
        );
        layout.add(cards);

        // ── продажи по месяцам (только оплаченные) ───────────────────
        Div salesSection = new Div();
        salesSection.addClassName("dashboard-section");
        salesSection.getStyle().set("margin-bottom", "16px");

        Div salesTitle = new Div();
        salesTitle.addClassName("section-title");
        salesTitle.setText("Продажи по месяцам (завершённые)");
        salesSection.add(salesTitle);

        Map<String, Double> salesByMonth = buildSalesByMonth(completed);
        salesSection.add(buildBarChart(salesByMonth));
        layout.add(salesSection);

        // ── распределение по жанрам (только оплаченные) ──────────────
        Map<String, Integer> genreCounts = buildGenreCounts(completed);
        int total = completed.size();

        Div genreSection = new Div();
        genreSection.addClassName("dashboard-section");

        Div genreTitle = new Div();
        genreTitle.addClassName("section-title");
        genreTitle.setText("Распределение по жанрам (завершённые)");
        genreSection.add(genreTitle);

        HorizontalLayout genreRow = new HorizontalLayout();
        genreRow.setWidthFull();
        genreRow.setAlignItems(Alignment.CENTER);
        genreRow.add(createPieChart(genreCounts, total));
        genreRow.add(buildGenreTable(genreCounts, total));
        genreSection.add(genreRow);
        layout.add(genreSection);

        return layout;
    }

    // ── карточка метрики ─────────────────────────────────────────────
    private Component card(String title, String value, String variant, String sub) {
        Div card = new Div();
        card.addClassName("highlight-card");
        card.addClassName(variant);
        card.getStyle().set("flex", "1").set("min-width", "160px");

        Div t = new Div(); t.addClassName("highlight-card-title"); t.setText(title);
        Div v = new Div(); v.addClassName("highlight-card-value"); v.setText(value);
        Div s = new Div(); s.addClassName("highlight-card-sub");   s.setText(sub);

        card.add(t, v, s);
        return card;
    }

    // ── данные по месяцам ─────────────────────────────────────────────
    private Map<String, Double> buildSalesByMonth(List<Purchase> list) {
        List<LocalDate> dates = list.stream()
                .map(Purchase::getPurchaseDate)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        Map<String, Double> result = new LinkedHashMap<>();
        for (LocalDate d : dates) {
            String m = d.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru"));
            result.putIfAbsent(m, 0.0);
        }
        for (Purchase p : list) {
            if (p.getPurchaseDate() == null) continue;
            String m = p.getPurchaseDate().getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru"));
            result.put(m, result.getOrDefault(m, 0.0) + p.getTotalAmount());
        }
        return result;
    }

    // ── горизонтальный бар-чарт ───────────────────────────────────────
    private Component buildBarChart(Map<String, Double> salesByMonth) {
        VerticalLayout chart = new VerticalLayout();
        chart.setPadding(false);
        chart.setSpacing(false);

        if (salesByMonth.isEmpty()) {
            Span empty = new Span("Нет данных");
            empty.getStyle().set("color", "#9e9e9e").set("font-size", "14px");
            chart.add(empty);
            return chart;
        }

        double max = salesByMonth.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);

        for (Map.Entry<String, Double> e : salesByMonth.entrySet()) {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(Alignment.CENTER);
            row.setWidthFull();
            row.getStyle().set("gap", "12px").set("margin-bottom", "8px");

            Span label = new Span(e.getKey());
            label.getStyle().set("width", "100px").set("font-size", "13px")
                    .set("color", "#616161").set("flex-shrink", "0");

            int pct = (int) (e.getValue() / max * 100);
            Div barWrap = new Div();
            barWrap.getStyle().set("flex", "1").set("background", "#f0f2f5")
                    .set("border-radius", "4px").set("height", "28px").set("position", "relative");

            Div bar = new Div();
            bar.getStyle()
                    .set("background", "linear-gradient(90deg,#1a73e8,#4a9eff)")
                    .set("width", pct + "%").set("height", "100%")
                    .set("border-radius", "4px").set("min-width", "4px")
                    .set("transition", "width 0.4s");
            barWrap.add(bar);

            Span val = new Span(String.format("%.0f ₽", e.getValue()));
            val.getStyle().set("font-size", "13px").set("font-weight", "600")
                    .set("color", "#212121").set("flex-shrink", "0").set("width", "90px");

            row.add(label, barWrap, val);
            chart.add(row);
        }
        return chart;
    }

    // ── данные по жанрам ─────────────────────────────────────────────
    private Map<String, Integer> buildGenreCounts(List<Purchase> list) {
        Map<Integer, Integer> gameToGenre = new HashMap<>();
        for (Game g : games) gameToGenre.put(g.getGameID(), g.getGenreID());

        Map<Integer, String> genreIdToName = new HashMap<>();
        for (Genre g : genres) genreIdToName.put(g.getGenreID(), g.getName());

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Purchase p : list) {
            Integer genreId = gameToGenre.get(p.getGameID());
            String name = genreIdToName.getOrDefault(genreId, "Прочее");
            result.put(name, result.getOrDefault(name, 0) + 1);
        }
        return result;
    }

    // ── таблица жанров ───────────────────────────────────────────────
    private Component buildGenreTable(Map<String, Integer> counts, int total) {
        VerticalLayout tbl = new VerticalLayout();
        tbl.setPadding(false);
        tbl.setSpacing(false);
        tbl.getStyle().set("min-width", "300px");

        int colorIdx = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            double pct = total > 0 ? (double) e.getValue() / total * 100 : 0;
            String color = getColor(colorIdx++);

            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(Alignment.CENTER);
            row.getStyle().set("gap", "10px").set("padding", "6px 0")
                    .set("border-bottom", "1px solid #f0f2f5");

            Div dot = new Div();
            dot.getStyle().set("width", "12px").set("height", "12px")
                    .set("background", color).set("border-radius", "50%").set("flex-shrink", "0");

            Span name = new Span(e.getKey());
            name.getStyle().set("font-size", "14px").set("flex", "1");

            Span cnt = new Span(e.getValue() + " шт.");
            cnt.getStyle().set("font-size", "13px").set("color", "#616161");

            Span pctSpan = new Span(String.format("%.1f%%", pct));
            pctSpan.getStyle().set("font-size", "13px").set("font-weight", "600")
                    .set("color", "#1a73e8").set("min-width", "45px").set("text-align", "right");

            row.add(dot, name, cnt, pctSpan);
            tbl.add(row);
        }
        return tbl;
    }

    // ── SVG круговая диаграмма ───────────────────────────────────────
    private Component createPieChart(Map<String, Integer> counts, int total) {
        if (total == 0) {
            Span empty = new Span("Нет данных");
            empty.getStyle().set("color", "#9e9e9e");
            return empty;
        }

        StringBuilder paths = new StringBuilder();
        double startAngle = -90.0;
        int colorIdx = 0;
        int cx = 130, cy = 130, r = 110, ri = 55;

        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            double pct = (double) e.getValue() / total;
            double sweep = pct * 360.0;
            double endAngle = startAngle + sweep;

            double x1o = cx + r * Math.cos(Math.toRadians(startAngle));
            double y1o = cy + r * Math.sin(Math.toRadians(startAngle));
            double x2o = cx + r * Math.cos(Math.toRadians(endAngle));
            double y2o = cy + r * Math.sin(Math.toRadians(endAngle));
            double x1i = cx + ri * Math.cos(Math.toRadians(endAngle));
            double y1i = cy + ri * Math.sin(Math.toRadians(endAngle));
            double x2i = cx + ri * Math.cos(Math.toRadians(startAngle));
            double y2i = cy + ri * Math.sin(Math.toRadians(startAngle));

            int la = sweep > 180 ? 1 : 0;
            String color = getColor(colorIdx++);

            paths.append(String.format(Locale.US,
                "<path d='M %.3f %.3f A %d %d 0 %d 1 %.3f %.3f L %.3f %.3f A %d %d 0 %d 0 %.3f %.3f Z' " +
                "fill='%s' stroke='white' stroke-width='3'/>",
                x1o, y1o, r, r, la, x2o, y2o, x1i, y1i, ri, ri, la, x2i, y2i, color));

            startAngle = endAngle;
        }

        String svg = String.format(
            "<svg width='260' height='260' viewBox='0 0 260 260'>%s" +
            "<circle cx='130' cy='130' r='45' fill='white'/>" +
            "<text x='130' y='125' text-anchor='middle' font-size='13' fill='#616161'>Итого</text>" +
            "<text x='130' y='145' text-anchor='middle' font-size='18' font-weight='bold' fill='#212121'>%d</text>" +
            "</svg>",
            paths, total);

        Div container = new Div();
        container.getElement().setProperty("innerHTML", svg);
        return container;
    }

    private String getColor(int i) {
        String[] colors = {"#1a73e8","#34a853","#fbbc04","#ea4335","#7b1fa2","#00796b","#c2185b","#ff6d00"};
        return colors[i % colors.length];
    }

    public void redraw() {
        removeAll();
        add(createDashboard());
    }
}

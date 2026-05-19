package org.triangle.base.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;

import org.triangle.base.Class.Model.*;

// Дашборд: метрики, бар-чарт продаж, пай-чарт по жанрам, топ игр
public class DashboardView extends VerticalLayout {

    private List<Game> games;
    private List<Purchase> purchases;
    private List<Customer> customers;
    private List<Developer> developers;
    private List<Genre> genres;

    // статусы считаются как реальная выручка (оплачено/завершено)
    private static final Set<String> PAID    = Set.of("Завершён", "Оплачено");
    private static final Set<String> PENDING = Set.of("В обработке", "Ожидание");

    public DashboardView(List<Purchase> purchases, List<Genre> genres, List<Game> games,
                         List<Customer> customers, List<Developer> developers) {
        this.games = games; this.customers = customers;
        this.purchases = purchases; this.developers = developers; this.genres = genres;
        setPadding(false); setSpacing(false);
        add(createDashboard());
    }

    private Component createDashboard() {
        VerticalLayout root = new VerticalLayout();
        root.addClassName("main-content");
        root.setPadding(false);
        root.setSpacing(false);
        root.getStyle().set("gap", "16px");

        // ── разбивка по статусу ──────────────────────────────────────
        List<Purchase> completed = filter(PAID);
        List<Purchase> pending   = filter(PENDING);
        List<Purchase> cancelled = purchases.stream()
                .filter(p -> "Отменено".equals(p.getStatus())).collect(Collectors.toList());

        double revenue    = completed.stream().mapToDouble(Purchase::getTotalAmount).sum();
        double pendingAmt = pending.stream().mapToDouble(Purchase::getTotalAmount).sum();
        int    total      = purchases.size();

        // ── карточки ─────────────────────────────────────────────────
        HorizontalLayout cards = new HorizontalLayout();
        cards.setWidthFull();
        cards.getStyle().set("gap", "14px").set("flex-wrap", "wrap").set("align-items", "stretch")
                        .set("padding", "0");

        double convRate = total > 0 ? completed.size() * 100.0 / total : 0;

        cards.add(
            metricCard("💰", "Выручка",           money(revenue),
                       "только оплаченные",        "card-success",
                       completed.size() * 100.0 / Math.max(1, total)),

            metricCard("🛒", "Завершено покупок",  String.valueOf(completed.size()),
                       String.format("%.0f%% конверсия", convRate), "card-primary",
                       convRate),

            metricCard("⏳", "В обработке",        String.valueOf(pending.size()),
                       pendingAmt > 0 ? money(pendingAmt) + " ожидает" : "нет ожидающих",
                       "card-warning",
                       total > 0 ? pending.size() * 100.0 / total : 0),

            metricCard("✕",  "Отменено",           String.valueOf(cancelled.size()),
                       total > 0 ? String.format("%.0f%% от всех", cancelled.size() * 100.0 / total) : "—",
                       "card-danger",
                       total > 0 ? cancelled.size() * 100.0 / total : 0),

            metricCard("👥", "Покупателей",        String.valueOf(customers.size()),
                       "активных клиентов",        "card-neutral", 100),

            metricCard("🎮", "Игр в каталоге",    String.valueOf(games.size()),
                       "позиций",                  "card-neutral", 100)
        );
        root.add(cards);

        // ── два блока рядом ───────────────────────────────────────────
        HorizontalLayout twoCol = new HorizontalLayout();
        twoCol.setWidthFull();
        twoCol.setAlignItems(Alignment.START);
        twoCol.getStyle().set("gap", "16px").set("align-items", "stretch");

        // левый блок — продажи по месяцам
        Div salesSection = new Div();
        salesSection.addClassName("dashboard-section");
        salesSection.getStyle().set("flex", "1");
        Div salesTitle = new Div(); salesTitle.addClassName("section-title");
        salesTitle.setText("📈  Продажи по месяцам");
        salesSection.add(salesTitle);
        Map<String, Double> salesByMonth = buildSalesByMonth(completed);
        salesSection.add(buildBarChart(salesByMonth));

        // правый блок — жанры
        Div genreSection = new Div();
        genreSection.addClassName("dashboard-section");
        genreSection.getStyle().set("flex", "1");
        Div genreTitle = new Div(); genreTitle.addClassName("section-title");
        genreTitle.setText("🥧  Распределение по жанрам");
        genreSection.add(genreTitle);

        Map<String, Integer> genreCounts = buildGenreCounts(completed);
        HorizontalLayout pieRow = new HorizontalLayout();
        pieRow.setAlignItems(Alignment.CENTER);
        pieRow.getStyle().set("gap", "24px").set("flex-wrap", "wrap");
        pieRow.add(createPieChart(genreCounts, completed.size()));
        pieRow.add(buildGenreTable(genreCounts, completed.size()));
        genreSection.add(pieRow);

        twoCol.add(salesSection, genreSection);
        root.add(twoCol);

        // ── топ-3 игры по выручке ─────────────────────────────────────
        Div topSection = new Div();
        topSection.addClassName("dashboard-section");
        Div topTitle = new Div(); topTitle.addClassName("section-title");
        topTitle.setText("🏆  Топ игр по выручке");
        topSection.add(topTitle);
        topSection.add(buildTopGames(completed));
        root.add(topSection);

        return root;
    }

    // ── карточка метрики ─────────────────────────────────────────────
    private Component metricCard(String icon, String title, String value,
                                  String sub, String variant, double progressPct) {
        Div card = new Div();
        card.addClassName("highlight-card");
        card.addClassName(variant);
        card.getStyle().set("flex", "1").set("min-width", "150px").set("position", "relative");

        // иконка в правом верхнем углу
        Div iconEl = new Div();
        iconEl.addClassName("metric-icon");
        iconEl.setText(icon);
        card.add(iconEl);

        Div t = new Div(); t.addClassName("highlight-card-title"); t.setText(title);
        Div v = new Div(); v.addClassName("highlight-card-value"); v.setText(value);
        Div s = new Div(); s.addClassName("highlight-card-sub");   s.setText(sub);

        // прогресс-бар внизу карточки
        Div progressWrap = new Div();
        progressWrap.addClassName("metric-progress-bg");
        Div progressBar = new Div();
        progressBar.addClassName("metric-progress-fill");
        progressBar.addClassName(variant);
        progressBar.getStyle().set("width", Math.min(100, progressPct) + "%");
        progressWrap.add(progressBar);

        card.add(t, v, s, progressWrap);
        return card;
    }

    // ── данные по месяцам ─────────────────────────────────────────────
    private Map<String, Double> buildSalesByMonth(List<Purchase> list) {
        List<LocalDate> dates = list.stream()
                .map(Purchase::getPurchaseDate).filter(Objects::nonNull)
                .sorted().collect(Collectors.toList());
        Map<String, Double> res = new LinkedHashMap<>();
        for (LocalDate d : dates) res.putIfAbsent(monthName(d), 0.0);
        for (Purchase p : list) {
            if (p.getPurchaseDate() == null) continue;
            String m = monthName(p.getPurchaseDate());
            res.put(m, res.getOrDefault(m, 0.0) + p.getTotalAmount());
        }
        return res;
    }

    private String monthName(LocalDate d) {
        return d.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("ru"));
    }

    // ── горизонтальный бар-чарт ───────────────────────────────────────
    private Component buildBarChart(Map<String, Double> data) {
        VerticalLayout chart = new VerticalLayout();
        chart.setPadding(false); chart.setSpacing(false);

        if (data.isEmpty()) {
            Span e = new Span("Нет данных");
            e.getStyle().set("color", "#9e9e9e").set("font-size", "14px");
            chart.add(e); return chart;
        }

        double max = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);

        for (Map.Entry<String, Double> e : data.entrySet()) {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(Alignment.CENTER);
            row.setWidthFull();
            row.getStyle().set("gap", "10px").set("margin-bottom", "10px");

            Span label = new Span(e.getKey());
            label.getStyle().set("width", "80px").set("font-size", "13px")
                    .set("color", "#6b7280").set("flex-shrink", "0");

            double pct = e.getValue() / max * 100;
            Div barWrap = new Div();
            barWrap.getStyle().set("flex", "1").set("background", "#f1f5f9")
                    .set("border-radius", "6px").set("height", "26px").set("overflow", "hidden");
            Div bar = new Div();
            bar.getStyle()
                    .set("background", "linear-gradient(90deg,#1a73e8 0%,#60a5fa 100%)")
                    .set("width", pct + "%").set("height", "100%")
                    .set("border-radius", "6px").set("min-width", pct > 0 ? "4px" : "0");
            barWrap.add(bar);

            Span val = new Span(money(e.getValue()));
            val.getStyle().set("font-size", "13px").set("font-weight", "700")
                    .set("color", "#1a1f36").set("flex-shrink", "0").set("min-width", "100px")
                    .set("text-align", "right");

            row.add(label, barWrap, val);
            chart.add(row);
        }
        return chart;
    }

    // ── жанры ─────────────────────────────────────────────────────────
    private Map<String, Integer> buildGenreCounts(List<Purchase> list) {
        Map<Integer, Integer> gameToGenre = new HashMap<>();
        for (Game g : games) gameToGenre.put(g.getGameID(), g.getGenreID());
        Map<Integer, String> genreIdToName = new HashMap<>();
        for (Genre g : genres) genreIdToName.put(g.getGenreID(), g.getName());
        Map<String, Integer> res = new LinkedHashMap<>();
        for (Purchase p : list) {
            Integer gId = gameToGenre.get(p.getGameID());
            res.merge(genreIdToName.getOrDefault(gId, "Прочее"), 1, Integer::sum);
        }
        // сортируем по убыванию
        return res.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    // ── таблица жанров ───────────────────────────────────────────────
    private Component buildGenreTable(Map<String, Integer> counts, int total) {
        VerticalLayout tbl = new VerticalLayout();
        tbl.setPadding(false); tbl.setSpacing(false);
        tbl.getStyle().set("min-width", "220px");

        int ci = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            double pct = total > 0 ? (double) e.getValue() / total * 100 : 0;
            String color = getColor(ci++);

            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(Alignment.CENTER);
            row.getStyle().set("gap", "10px").set("padding", "7px 0")
                    .set("border-bottom", "1px solid #f1f5f9");

            Div dot = new Div();
            dot.getStyle().set("width", "10px").set("height", "10px").set("background", color)
                    .set("border-radius", "50%").set("flex-shrink", "0");

            Span name = new Span(e.getKey());
            name.getStyle().set("font-size", "13px").set("flex", "1");

            Span cnt = new Span(e.getValue() + " шт.");
            cnt.getStyle().set("font-size", "12px").set("color", "#6b7280");

            Span pctS = new Span(String.format("%.1f%%", pct));
            pctS.getStyle().set("font-size", "13px").set("font-weight", "700")
                    .set("color", "#1a73e8").set("min-width", "42px").set("text-align", "right");

            row.add(dot, name, cnt, pctS);
            tbl.add(row);
        }
        return tbl;
    }

    // ── топ игр ───────────────────────────────────────────────────────
    private Component buildTopGames(List<Purchase> list) {
        Map<Integer, Double> rev = new HashMap<>();
        Map<Integer, String> titles = new HashMap<>();
        for (Game g : games) titles.put(g.getGameID(), g.getTitle());
        for (Purchase p : list) rev.merge(p.getGameID(), p.getTotalAmount(), Double::sum);

        List<Map.Entry<Integer, Double>> sorted = rev.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(5).collect(Collectors.toList());

        if (sorted.isEmpty()) {
            Span e = new Span("Нет данных");
            e.getStyle().set("color", "#9e9e9e"); return e;
        }

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.getStyle().set("gap", "14px").set("flex-wrap", "wrap");

        String[] medals = {"🥇", "🥈", "🥉", "4.", "5."};
        int idx = 0;
        for (Map.Entry<Integer, Double> e : sorted) {
            String title = titles.getOrDefault(e.getKey(), "Игра #" + e.getKey());
            Div card = new Div();
            card.addClassName("top-game-card");
            card.getStyle().set("flex", "1").set("min-width", "140px");

            Div medal = new Div(); medal.addClassName("top-game-medal");
            medal.setText(medals[idx]);
            Div name = new Div(); name.addClassName("top-game-name"); name.setText(title);
            Div amount = new Div(); amount.addClassName("top-game-amount"); amount.setText(money(e.getValue()));

            card.add(medal, name, amount);
            row.add(card);
            idx++;
        }
        return row;
    }

    // ── SVG donut без внешних библиотек: рисуем path дуги через тригонометрию ──
    private Component createPieChart(Map<String, Integer> counts, int total) {
        if (total == 0) { Span e = new Span("Нет данных"); e.getStyle().set("color","#9e9e9e"); return e; }

        // cx,cy — центр; r — внешний радиус; ri — внутренний (отверстие пончика)
        int cx = 120, cy = 120, r = 100, ri = 52;
        double midR = (r + ri) / 2.0; // радиус для подписи процента внутри сектора

        StringBuilder slices = new StringBuilder();
        StringBuilder labels = new StringBuilder();
        double start = -90.0;
        int ci = 0;

        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            double pct   = (double) e.getValue() / total;
            double sweep = pct * 360.0;
            double end   = start + sweep;
            int    la    = sweep > 180 ? 1 : 0;
            String color = getColor(ci++);

            // координаты дуги
            double x1o = cx + r  * Math.cos(Math.toRadians(start));
            double y1o = cy + r  * Math.sin(Math.toRadians(start));
            double x2o = cx + r  * Math.cos(Math.toRadians(end));
            double y2o = cy + r  * Math.sin(Math.toRadians(end));
            double x1i = cx + ri * Math.cos(Math.toRadians(end));
            double y1i = cy + ri * Math.sin(Math.toRadians(end));
            double x2i = cx + ri * Math.cos(Math.toRadians(start));
            double y2i = cy + ri * Math.sin(Math.toRadians(start));

            slices.append(String.format(Locale.US,
                "<path d='M%.2f %.2f A%d %d 0 %d 1 %.2f %.2f L%.2f %.2f A%d %d 0 %d 0 %.2f %.2f Z'" +
                " fill='%s' stroke='white' stroke-width='3.5'/>",
                x1o, y1o, r, r, la, x2o, y2o,
                x1i, y1i, ri, ri, la, x2i, y2i, color));

            // процент внутри сектора — только если места хватает (>= 7%)
            if (pct >= 0.07) {
                double mid = start + sweep / 2.0;
                double tx  = cx + midR * Math.cos(Math.toRadians(mid));
                double ty  = cy + midR * Math.sin(Math.toRadians(mid));
                String label = String.format("%.0f%%", pct * 100);
                labels.append(String.format(Locale.US,
                    "<text x='%.1f' y='%.1f' text-anchor='middle' dominant-baseline='central'" +
                    " font-size='12' font-weight='700' fill='white'" +
                    " font-family='Inter,sans-serif' style='text-shadow:0 1px 3px rgba(0,0,0,0.4)'>%s</text>",
                    tx, ty, label));
            }

            start = end;
        }

        String svg = String.format(
            "<svg width='240' height='240' viewBox='0 0 240 240'>" +
            "%s%s" +
            "<circle cx='%d' cy='%d' r='46' fill='white'/>" +
            "<text x='%d' y='%d' text-anchor='middle' font-size='11' fill='#6b7280'" +
            " font-family='Inter,sans-serif'>Итого</text>" +
            "<text x='%d' y='%d' text-anchor='middle' font-size='22' font-weight='800'" +
            " fill='#1a1f36' font-family='Inter,sans-serif'>%d</text>" +
            "</svg>",
            slices, labels,
            cx, cy, cx, cy - 8, cx, cy + 14, total);

        Div c = new Div();
        c.getElement().setProperty("innerHTML", svg);
        return c;
    }

    // ── helpers ───────────────────────────────────────────────────────
    private List<Purchase> filter(Set<String> statuses) {
        return purchases.stream().filter(p -> p.getStatus() != null && statuses.contains(p.getStatus()))
                .collect(Collectors.toList());
    }

    private String money(double v) {
        // формат: "26 300,00 ₽" по-русски
        String raw = String.format(Locale.US, "%,.2f", v); // "26,300.00"
        String[] parts = raw.split("\\.");
        String intPart  = parts[0].replace(",", " "); // неразрывный пробел как разделитель тысяч
        String fracPart = parts.length > 1 ? parts[1] : "00";
        return intPart + "," + fracPart + " ₽";
    }

    private String getColor(int i) {
        String[] c = {"#1a73e8","#34a853","#fbbc04","#ea4335","#7b1fa2","#00796b","#c2185b","#ff6d00"};
        return c[i % c.length];
    }

    public void redraw() { removeAll(); add(createDashboard()); }
}

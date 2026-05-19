package org.triangle.base.ui.view.Dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;

import org.triangle.base.Class.Model.Developer;
import org.triangle.base.Class.Model.Game;
import org.triangle.base.Class.Model.Genre;
import org.triangle.base.ui.service.GameService;

import java.sql.SQLException;
import java.util.List;

// Открывает диалоги добавления / редактирования / удаления для таблицы Игры
public class GameDialogManager {
    private final GameService gameService;
    private final Grid<Game> grid;
    private final List<Genre> genres;       // для ComboBox жанров
    private final List<Developer> developers; // для ComboBox разработчиков

    public GameDialogManager(GameService gameService, Grid<Game> grid,
                             List<Genre> genres, List<Developer> developers) {
        this.gameService = gameService;
        this.grid = grid;
        this.genres = genres;
        this.developers = developers;
    }

    public void openAddGameDialog(List<Game> games) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Добавить игру");

        TextField titleField = new TextField("Название");
        TextField descField = new TextField("Описание");
        NumberField priceField = new NumberField("Цена");
        NumberField stockField = new NumberField("Количество на складе");

        ComboBox<Genre> genreBox = new ComboBox<>("Жанр");
        genreBox.setItems(genres);
        genreBox.setItemLabelGenerator(Genre::getName);

        ComboBox<Developer> devBox = new ComboBox<>("Разработчик");
        devBox.setItems(developers);
        devBox.setItemLabelGenerator(Developer::getName);

        VerticalLayout layout = new VerticalLayout(titleField, descField, priceField, stockField, genreBox, devBox);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            try {
                if (titleField.isEmpty() || priceField.isEmpty() || genreBox.isEmpty() || devBox.isEmpty()) {
                    Notification.show("Заполните все обязательные поля");
                    return;
                }
                // ID = max существующего + 1, чтобы не было дублей
                int newID = games.stream().mapToInt(Game::getGameID).max().orElse(0) + 1;
                Game game = new Game(
                        newID,
                        titleField.getValue(),
                        descField.getValue(),
                        priceField.getValue(),
                        stockField.isEmpty() ? 0 : stockField.getValue().intValue(),
                        genreBox.getValue().getGenreID(),
                        devBox.getValue().getDeveloperID()
                );
                gameService.createGame(game);
                games.add(game);
                grid.setItems(games);
                Notification.show("Игра добавлена");
                dialog.close();
            } catch (SQLException e) {
                Notification.show("Ошибка: " + e.getMessage());
            }
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        dialog.add(new HorizontalLayout(saveButton, cancelButton));
        dialog.open();
    }

    public void openEditGameDialog() {
        Game selected = grid.asSingleSelect().getValue();
        if (selected == null) {
            Notification.show("Выберите игру для изменения");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Изменить игру");

        TextField titleField = new TextField("Название");
        titleField.setValue(selected.getTitle());

        TextField descField = new TextField("Описание");
        descField.setValue(selected.getDescription() != null ? selected.getDescription() : "");

        NumberField priceField = new NumberField("Цена");
        priceField.setValue(selected.getPrice());

        NumberField stockField = new NumberField("Количество на складе");
        stockField.setValue((double) selected.getStockQuantity());

        ComboBox<Genre> genreBox = new ComboBox<>("Жанр");
        genreBox.setItems(genres);
        genreBox.setItemLabelGenerator(Genre::getName);
        // предзаполняем ComboBox текущим значением выбранной строки
        genres.stream().filter(g -> g.getGenreID() == selected.getGenreID()).findFirst()
                .ifPresent(genreBox::setValue);

        ComboBox<Developer> devBox = new ComboBox<>("Разработчик");
        devBox.setItems(developers);
        devBox.setItemLabelGenerator(Developer::getName);
        developers.stream().filter(d -> d.getDeveloperID() == selected.getDeveloperID()).findFirst()
                .ifPresent(devBox::setValue);

        VerticalLayout layout = new VerticalLayout(titleField, descField, priceField, stockField, genreBox, devBox);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            try {
                if (genreBox.isEmpty() || devBox.isEmpty()) {
                    Notification.show("Выберите жанр и разработчика");
                    return;
                }
                selected.setTitle(titleField.getValue());
                selected.setDescription(descField.getValue());
                selected.setPrice(priceField.getValue());
                selected.setStockQuantity(stockField.getValue().intValue());
                selected.setGenreID(genreBox.getValue().getGenreID());
                selected.setDeveloperID(devBox.getValue().getDeveloperID());
                gameService.updateGame(selected);
                grid.getDataProvider().refreshAll();
                Notification.show("Игра обновлена");
                dialog.close();
            } catch (SQLException e) {
                Notification.show("Ошибка: " + e.getMessage());
            }
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        dialog.add(new HorizontalLayout(saveButton, cancelButton));
        dialog.open();
    }

    public void openDelGameDialog(List<Game> games) {
        Game selected = grid.asSingleSelect().getValue();
        if (selected == null) {
            Notification.show("Выберите игру для удаления");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Удалить игру?");
        dialog.add("Вы уверены, что хотите удалить: " + selected.getTitle() + "?");

        Button confirmButton = new Button("Удалить", event -> {
            try {
                gameService.deleteGame(selected.getGameID());
                games.remove(selected);
                grid.setItems(games);
                Notification.show("Игра удалена");
                dialog.close();
            } catch (SQLException e) {
                Notification.show("Ошибка: " + e.getMessage());
            }
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        dialog.add(new HorizontalLayout(confirmButton, cancelButton));
        dialog.open();
    }
}

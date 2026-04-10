package org.triangle.base.ui.view.Dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;

import org.triangle.base.Class.Model.Game;
import org.triangle.base.ui.service.GameService;

import java.sql.SQLException;
import java.util.List;

public class GameDialogManager {
    private final GameService gameService;
    private final Grid<Game> grid;

    public GameDialogManager(GameService gameService, Grid<Game> grid) {
        this.gameService = gameService;
        this.grid = grid;
    }

    public void openAddGameDialog(List<Game> games) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Добавить игру");

        NumberField gameIDField = new NumberField("Код игры");
        TextField titleField = new TextField("Название");
        TextField descField = new TextField("Описание");
        NumberField priceField = new NumberField("Цена");
        NumberField stockField = new NumberField("Количество");
        NumberField genreField = new NumberField("Код жанра");
        NumberField devField = new NumberField("Код разработчика");

        VerticalLayout layout = new VerticalLayout(gameIDField, titleField, descField, priceField, stockField, genreField, devField);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            try {
                if (titleField.isEmpty() || priceField.isEmpty()) {
                    Notification.show("Заполните все обязательные поля");
                    return;
                }
                Game game = new Game(
                        gameIDField.getValue().intValue(),
                        titleField.getValue(),
                        descField.getValue(),
                        priceField.getValue(),
                        stockField.getValue().intValue(),
                        genreField.getValue().intValue(),
                        devField.getValue().intValue()
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
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(buttons);
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

        NumberField gameIDField = new NumberField("Код игры");
        gameIDField.setValue((double) selected.getGameID());
        gameIDField.setReadOnly(true);

        TextField titleField = new TextField("Название");
        titleField.setValue(selected.getTitle());

        TextField descField = new TextField("Описание");
        descField.setValue(selected.getDescription() != null ? selected.getDescription() : "");

        NumberField priceField = new NumberField("Цена");
        priceField.setValue(selected.getPrice());

        NumberField stockField = new NumberField("Количество");
        stockField.setValue((double) selected.getStockQuantity());

        NumberField genreField = new NumberField("Код жанра");
        genreField.setValue((double) selected.getGenreID());

        NumberField devField = new NumberField("Код разработчика");
        devField.setValue((double) selected.getDeveloperID());

        VerticalLayout layout = new VerticalLayout(gameIDField, titleField, descField, priceField, stockField, genreField, devField);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            try {
                selected.setTitle(titleField.getValue());
                selected.setDescription(descField.getValue());
                selected.setPrice(priceField.getValue());
                selected.setStockQuantity(stockField.getValue().intValue());
                selected.setGenreID(genreField.getValue().intValue());
                selected.setDeveloperID(devField.getValue().intValue());
                gameService.updateGame(selected);
                grid.getDataProvider().refreshAll();
                Notification.show("Игра обновлена");
                dialog.close();
            } catch (SQLException e) {
                Notification.show("Ошибка: " + e.getMessage());
            }
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(buttons);
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
        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        dialog.add(buttons);
        dialog.open();
    }
}

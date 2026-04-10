package org.triangle.base.ui.view.Dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;

import org.triangle.base.Class.Model.Genre;
import org.triangle.base.ui.service.GenreService;

import java.sql.SQLException;
import java.util.List;

public class GenreDialogManager {
    private final GenreService genreService;
    private final Grid<Genre> grid;

    public GenreDialogManager(GenreService genreService, Grid<Genre> grid) {
        this.genreService = genreService;
        this.grid = grid;
    }

    public void openAddGenreDialog(List<Genre> genres) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Добавить жанр");

        TextField nameField = new TextField("Название");
        TextField descField = new TextField("Описание");

        VerticalLayout layout = new VerticalLayout(nameField, descField);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            if (nameField.isEmpty()) {
                Notification.show("Заполните название жанра");
                return;
            }
            Genre genre = new Genre(0, nameField.getValue(), descField.getValue());
            genreService.createGenre(genre);
            genres.add(genre);
            grid.setItems(genres);
            Notification.show("Жанр добавлен");
            dialog.close();
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(buttons);
        dialog.open();
    }

    public void openEditGenreDialog() {
        Genre selected = grid.asSingleSelect().getValue();
        if (selected == null) {
            Notification.show("Выберите жанр для изменения");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Изменить жанр");

        TextField nameField = new TextField("Название");
        nameField.setValue(selected.getName());

        TextField descField = new TextField("Описание");
        descField.setValue(selected.getDescription() != null ? selected.getDescription() : "");

        VerticalLayout layout = new VerticalLayout(nameField, descField);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            selected.setName(nameField.getValue());
            selected.setDescription(descField.getValue());
            genreService.updateGenre(selected);
            grid.getDataProvider().refreshAll();
            Notification.show("Жанр обновлен");
            dialog.close();
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(buttons);
        dialog.open();
    }

    public void openDelGenreDialog() {
        Genre selected = grid.asSingleSelect().getValue();
        if (selected == null) {
            Notification.show("Выберите жанр для удаления");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Удалить жанр?");
        dialog.add("Вы уверены, что хотите удалить: " + selected.getName() + "?");

        Button confirmButton = new Button("Удалить", event -> {
            try {
                genreService.deleteGenre(selected.getGenreID());
                List<Genre> genres = genreService.getGenres();
                grid.setItems(genres);
                Notification.show("Жанр удален");
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

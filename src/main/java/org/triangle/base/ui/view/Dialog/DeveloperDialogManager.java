package org.triangle.base.ui.view.Dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import org.triangle.base.Class.Model.Developer;
import org.triangle.base.ui.service.DeveloperService;

import java.sql.SQLException;
import java.util.List;

public class DeveloperDialogManager {
    private final DeveloperService developerService;
    private final Grid<Developer> grid;

    public DeveloperDialogManager(DeveloperService developerService, Grid<Developer> grid) {
        this.developerService = developerService;
        this.grid = grid;
    }

    public void openAddDeveloperDialog(List<Developer> developers) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Добавить разработчика");

        TextField nameField = new TextField("Название компании");
        TextField contactField = new TextField("Контактное имя");
        TextField phoneField = new TextField("Телефон");
        TextField emailField = new TextField("Email");
        TextField addressField = new TextField("Адрес");

        VerticalLayout layout = new VerticalLayout(nameField, contactField, phoneField, emailField, addressField);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            if (nameField.isEmpty()) {
                Notification.show("Заполните название компании");
                return;
            }
            Developer developer = new Developer(
                    0, nameField.getValue(), contactField.getValue(),
                    phoneField.getValue(), emailField.getValue(), addressField.getValue()
            );
            developerService.createDeveloper(developer);
            developers.add(developer);
            grid.setItems(developers);
            Notification.show("Разработчик добавлен");
            dialog.close();
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(buttons);
        dialog.open();
    }

    public void openEditDeveloperDialog() {
        Developer selected = grid.asSingleSelect().getValue();
        if (selected == null) {
            Notification.show("Выберите разработчика для изменения");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Изменить разработчика");

        TextField nameField = new TextField("Название компании");
        nameField.setValue(selected.getName());

        TextField contactField = new TextField("Контактное имя");
        contactField.setValue(selected.getContactName() != null ? selected.getContactName() : "");

        TextField phoneField = new TextField("Телефон");
        phoneField.setValue(selected.getPhoneNumber() != null ? selected.getPhoneNumber() : "");

        TextField emailField = new TextField("Email");
        emailField.setValue(selected.getEmail() != null ? selected.getEmail() : "");

        TextField addressField = new TextField("Адрес");
        addressField.setValue(selected.getAddress() != null ? selected.getAddress() : "");

        VerticalLayout layout = new VerticalLayout(nameField, contactField, phoneField, emailField, addressField);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            selected.setName(nameField.getValue());
            selected.setContactName(contactField.getValue());
            selected.setPhoneNumber(phoneField.getValue());
            selected.setEmail(emailField.getValue());
            selected.setAddress(addressField.getValue());
            developerService.updateDeveloper(selected);
            grid.getDataProvider().refreshAll();
            Notification.show("Разработчик обновлен");
            dialog.close();
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(buttons);
        dialog.open();
    }

    public void openDelDeveloperDialog(List<Developer> developers) {
        Developer selected = grid.asSingleSelect().getValue();
        if (selected == null) {
            Notification.show("Выберите разработчика для удаления");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Удалить разработчика?");
        dialog.add("Вы уверены, что хотите удалить: " + selected.getName() + "?");

        Button confirmButton = new Button("Удалить", event -> {
            try {
                developerService.deleteDeveloper(selected.getDeveloperID());
                developers.remove(selected);
                grid.setItems(developers);
                Notification.show("Разработчик удален");
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

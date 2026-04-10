package org.triangle.base.ui.view.Dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;

import org.triangle.base.Class.Model.Customer;
import org.triangle.base.ui.service.CustomerService;

import java.sql.SQLException;
import java.util.List;

public class CustomerDialogManager {
    private final CustomerService customerService;
    private final Grid<Customer> grid;

    public CustomerDialogManager(CustomerService customerService, Grid<Customer> grid) {
        this.customerService = customerService;
        this.grid = grid;
    }

    public void openAddCustomerDialog(List<Customer> customers) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Добавить покупателя");

        NumberField idField = new NumberField("Код покупателя");
        TextField nameField = new TextField("ФИО");
        TextField emailField = new TextField("Email");
        TextField phoneField = new TextField("Телефон");
        TextField addressField = new TextField("Адрес");
        DatePicker datePicker = new DatePicker("Дата регистрации");

        VerticalLayout layout = new VerticalLayout(idField, nameField, emailField, phoneField, addressField, datePicker);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            if (nameField.isEmpty() || idField.isEmpty()) {
                Notification.show("Заполните обязательные поля");
                return;
            }
            Customer customer = new Customer(
                    idField.getValue().intValue(),
                    nameField.getValue(),
                    emailField.getValue(),
                    phoneField.getValue(),
                    addressField.getValue(),
                    datePicker.getValue()
            );
            customerService.createCustomer(customer);
            customers.add(customer);
            grid.setItems(customers);
            Notification.show("Покупатель добавлен");
            dialog.close();
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(buttons);
        dialog.open();
    }

    public void openEditCustomerDialog() {
        Customer selected = grid.asSingleSelect().getValue();
        if (selected == null) {
            Notification.show("Выберите покупателя для изменения");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Изменить покупателя");

        NumberField idField = new NumberField("Код покупателя");
        idField.setValue((double) selected.getCustomerID());
        idField.setReadOnly(true);

        TextField nameField = new TextField("ФИО");
        nameField.setValue(selected.getName());

        TextField emailField = new TextField("Email");
        emailField.setValue(selected.getEmail() != null ? selected.getEmail() : "");

        TextField phoneField = new TextField("Телефон");
        phoneField.setValue(selected.getPhoneNumber() != null ? selected.getPhoneNumber() : "");

        TextField addressField = new TextField("Адрес");
        addressField.setValue(selected.getAddress() != null ? selected.getAddress() : "");

        DatePicker datePicker = new DatePicker("Дата регистрации");
        datePicker.setValue(selected.getRegistrationDate());

        VerticalLayout layout = new VerticalLayout(idField, nameField, emailField, phoneField, addressField, datePicker);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            selected.setName(nameField.getValue());
            selected.setEmail(emailField.getValue());
            selected.setPhoneNumber(phoneField.getValue());
            selected.setAddress(addressField.getValue());
            selected.setRegistrationDate(datePicker.getValue());
            customerService.updateCustomer(selected);
            grid.getDataProvider().refreshAll();
            Notification.show("Покупатель обновлен");
            dialog.close();
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(buttons);
        dialog.open();
    }

    public void openDelCustomerDialog() {
        Customer selected = grid.asSingleSelect().getValue();
        if (selected == null) {
            Notification.show("Выберите покупателя для удаления");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Удалить покупателя?");
        dialog.add("Вы уверены, что хотите удалить: " + selected.getName() + "?");

        Button confirmButton = new Button("Удалить", event -> {
            try {
                customerService.deleteCustomer(selected.getCustomerID());
                List<Customer> customers = customerService.getCustomers();
                grid.setItems(customers);
                Notification.show("Покупатель удален");
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

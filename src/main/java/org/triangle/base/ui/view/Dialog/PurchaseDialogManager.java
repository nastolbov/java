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

import org.triangle.base.Class.Model.Purchase;
import org.triangle.base.ui.service.PurchaseService;

import java.sql.SQLException;
import java.util.List;

public class PurchaseDialogManager {
    private final PurchaseService purchaseService;
    private final Grid<Purchase> grid;

    public PurchaseDialogManager(PurchaseService purchaseService, Grid<Purchase> grid) {
        this.purchaseService = purchaseService;
        this.grid = grid;
    }

    public void openAddPurchaseDialog(List<Purchase> purchases) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Добавить покупку");

        NumberField customerIDField = new NumberField("Код покупателя");
        DatePicker datePicker = new DatePicker("Дата покупки");
        NumberField totalField = new NumberField("Итоговая сумма");
        TextField statusField = new TextField("Статус");
        NumberField gameIDField = new NumberField("Код игры");
        NumberField countField = new NumberField("Количество");

        VerticalLayout layout = new VerticalLayout(customerIDField, datePicker, totalField, statusField, gameIDField, countField);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            if (customerIDField.isEmpty() || gameIDField.isEmpty()) {
                Notification.show("Заполните обязательные поля");
                return;
            }
            Purchase purchase = new Purchase(
                    0,
                    customerIDField.getValue().intValue(),
                    datePicker.getValue(),
                    totalField.getValue() != null ? totalField.getValue() : 0.0,
                    statusField.getValue(),
                    gameIDField.getValue().intValue(),
                    countField.getValue() != null ? countField.getValue().intValue() : 1
            );
            purchaseService.createPurchase(purchase);
            purchases.add(purchase);
            grid.setItems(purchases);
            Notification.show("Покупка добавлена");
            dialog.close();
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(buttons);
        dialog.open();
    }

    public void openEditPurchaseDialog() {
        Purchase selected = grid.asSingleSelect().getValue();
        if (selected == null) {
            Notification.show("Выберите покупку для изменения");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Изменить покупку");

        NumberField idField = new NumberField("Код покупки");
        idField.setValue((double) selected.getPurchaseID());
        idField.setReadOnly(true);

        NumberField customerIDField = new NumberField("Код покупателя");
        customerIDField.setValue((double) selected.getCustomerID());

        DatePicker datePicker = new DatePicker("Дата покупки");
        datePicker.setValue(selected.getPurchaseDate());

        NumberField totalField = new NumberField("Итоговая сумма");
        totalField.setValue(selected.getTotalAmount());

        TextField statusField = new TextField("Статус");
        statusField.setValue(selected.getStatus() != null ? selected.getStatus() : "");

        NumberField gameIDField = new NumberField("Код игры");
        gameIDField.setValue((double) selected.getGameID());

        NumberField countField = new NumberField("Количество");
        countField.setValue((double) selected.getCount());

        VerticalLayout layout = new VerticalLayout(idField, customerIDField, datePicker, totalField, statusField, gameIDField, countField);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            selected.setCustomerID(customerIDField.getValue().intValue());
            selected.setPurchaseDate(datePicker.getValue());
            selected.setTotalAmount(totalField.getValue());
            selected.setStatus(statusField.getValue());
            selected.setGameID(gameIDField.getValue().intValue());
            selected.setCount(countField.getValue().intValue());
            purchaseService.updatePurchase(selected);
            grid.getDataProvider().refreshAll();
            Notification.show("Покупка обновлена");
            dialog.close();
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(buttons);
        dialog.open();
    }

    public void openDelPurchaseDialog(List<Purchase> purchases) {
        Purchase selected = grid.asSingleSelect().getValue();
        if (selected == null) {
            Notification.show("Выберите покупку для удаления");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Удалить покупку?");
        dialog.add("Вы уверены, что хотите удалить покупку #" + selected.getPurchaseID() + "?");

        Button confirmButton = new Button("Удалить", event -> {
            try {
                purchaseService.deletePurchase(selected.getPurchaseID());
                purchases.remove(selected);
                grid.setItems(purchases);
                Notification.show("Покупка удалена");
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

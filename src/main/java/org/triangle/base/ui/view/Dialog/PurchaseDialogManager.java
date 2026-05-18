package org.triangle.base.ui.view.Dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;

import org.triangle.base.Class.Model.Customer;
import org.triangle.base.Class.Model.Game;
import org.triangle.base.Class.Model.Purchase;
import org.triangle.base.ui.service.PurchaseService;

import java.time.LocalDate;
import java.util.List;

public class PurchaseDialogManager {
    private final PurchaseService purchaseService;
    private final Grid<Purchase> grid;
    private final List<Customer> customers;
    private final List<Game> games;
    private final List<Purchase> purchases;

    public PurchaseDialogManager(PurchaseService purchaseService, Grid<Purchase> grid,
                                 List<Customer> customers, List<Game> games,
                                 List<Purchase> purchases) {
        this.purchaseService = purchaseService;
        this.grid = grid;
        this.customers = customers;
        this.games = games;
        this.purchases = purchases;
    }

    public void openAddPurchaseDialog(List<Purchase> purchases) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Добавить покупку");

        ComboBox<Customer> customerBox = new ComboBox<>("Покупатель");
        customerBox.setItems(customers);
        customerBox.setItemLabelGenerator(Customer::getName);

        ComboBox<Game> gameBox = new ComboBox<>("Игра");
        gameBox.setItems(games);
        gameBox.setItemLabelGenerator(Game::getTitle);

        DatePicker datePicker = new DatePicker("Дата покупки");
        datePicker.setValue(LocalDate.now());

        NumberField totalField = new NumberField("Итоговая сумма");

        NumberField countField = new NumberField("Количество");
        countField.setValue(1.0);
        countField.setMin(1);

        // авторасчёт суммы
        gameBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                double count = countField.isEmpty() ? 1.0 : countField.getValue();
                totalField.setValue(e.getValue().getPrice() * count);
            }
        });
        countField.addValueChangeListener(e -> {
            if (e.getValue() != null && gameBox.getValue() != null) {
                totalField.setValue(gameBox.getValue().getPrice() * e.getValue());
            }
        });

        ComboBox<String> statusBox = new ComboBox<>("Статус");
        statusBox.setItems("Оплачено", "Ожидание", "Отменено");
        statusBox.setValue("Оплачено");

        VerticalLayout layout = new VerticalLayout(customerBox, gameBox, datePicker, countField, totalField, statusBox);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            if (customerBox.isEmpty() || gameBox.isEmpty()) {
                Notification.show("Выберите покупателя и игру");
                return;
            }
            Purchase purchase = new Purchase(
                    0,
                    customerBox.getValue().getCustomerID(),
                    datePicker.getValue(),
                    totalField.isEmpty() ? 0.0 : totalField.getValue(),
                    statusBox.getValue(),
                    gameBox.getValue().getGameID(),
                    countField.isEmpty() ? 1 : countField.getValue().intValue()
            );
            purchaseService.createPurchase(purchase);
            purchases.add(purchase);
            grid.setItems(purchases);
            Notification.show("Покупка добавлена");
            dialog.close();
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        dialog.add(new HorizontalLayout(saveButton, cancelButton));
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

        ComboBox<Customer> customerBox = new ComboBox<>("Покупатель");
        customerBox.setItems(customers);
        customerBox.setItemLabelGenerator(Customer::getName);
        customers.stream().filter(c -> c.getCustomerID() == selected.getCustomerID()).findFirst()
                .ifPresent(customerBox::setValue);

        ComboBox<Game> gameBox = new ComboBox<>("Игра");
        gameBox.setItems(games);
        gameBox.setItemLabelGenerator(Game::getTitle);
        games.stream().filter(g -> g.getGameID() == selected.getGameID()).findFirst()
                .ifPresent(gameBox::setValue);

        DatePicker datePicker = new DatePicker("Дата покупки");
        datePicker.setValue(selected.getPurchaseDate());

        NumberField countField = new NumberField("Количество");
        countField.setValue((double) selected.getCount());
        countField.setMin(1);

        NumberField totalField = new NumberField("Итоговая сумма");
        totalField.setValue(selected.getTotalAmount());

        // авторасчёт суммы при изменении игры или количества
        gameBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                double count = countField.isEmpty() ? 1.0 : countField.getValue();
                totalField.setValue(e.getValue().getPrice() * count);
            }
        });
        countField.addValueChangeListener(e -> {
            if (e.getValue() != null && gameBox.getValue() != null) {
                totalField.setValue(gameBox.getValue().getPrice() * e.getValue());
            }
        });

        ComboBox<String> statusBox = new ComboBox<>("Статус");
        statusBox.setItems("Завершён", "Оплачено", "В обработке", "Ожидание", "Отменено");
        if (selected.getStatus() != null) statusBox.setValue(selected.getStatus());

        VerticalLayout layout = new VerticalLayout(customerBox, gameBox, datePicker, countField, totalField, statusBox);
        dialog.add(layout);

        Button saveButton = new Button("Сохранить", event -> {
            if (customerBox.isEmpty() || gameBox.isEmpty()) {
                Notification.show("Выберите покупателя и игру");
                return;
            }
            selected.setCustomerID(customerBox.getValue().getCustomerID());
            selected.setGameID(gameBox.getValue().getGameID());
            selected.setPurchaseDate(datePicker.getValue());
            selected.setCount(countField.isEmpty() ? 1 : countField.getValue().intValue());
            selected.setTotalAmount(totalField.isEmpty() ? 0.0 : totalField.getValue());
            selected.setStatus(statusBox.getValue());
            purchaseService.updatePurchase(selected);
            // setItems надёжнее чем refreshAll для ComponentRenderer
            grid.setItems(purchases);
            Notification.show("Покупка обновлена");
            dialog.close();
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        dialog.add(new HorizontalLayout(saveButton, cancelButton));
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
            } catch (Exception e) {
                Notification.show("Ошибка: " + e.getMessage());
            }
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());
        dialog.add(new HorizontalLayout(confirmButton, cancelButton));
        dialog.open();
    }
}

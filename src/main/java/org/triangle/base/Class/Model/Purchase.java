package org.triangle.base.Class.Model;

import java.time.LocalDate;

public class Purchase {
    private int purchaseID;
    private int customerID;
    private LocalDate purchaseDate;
    private double totalAmount;
    private String status;
    private int gameID;
    private int count;

    public Purchase(int purchaseID, int customerID, LocalDate purchaseDate, double totalAmount, String status, int gameID, int count) {
        this.purchaseID = purchaseID;
        this.customerID = customerID;
        this.purchaseDate = purchaseDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.gameID = gameID;
        this.count = count;
    }

    public int getPurchaseID() {
        return purchaseID;
    }

    public void setPurchaseID(int purchaseID) {
        this.purchaseID = purchaseID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

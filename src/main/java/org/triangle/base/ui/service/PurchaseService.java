package org.triangle.base.ui.service;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.triangle.base.Class.Model.Purchase;

@Service
public class PurchaseService {
    private static final String URL = "jdbc:h2:mem:computer_games";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public List<Purchase> getPurchases() {
        List<Purchase> purchases = new ArrayList<>();
        String query = "SELECT * FROM Purchase";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                java.sql.Date sqlDate = rs.getDate("PurchaseDate");
                LocalDate purchaseDate = sqlDate != null ? sqlDate.toLocalDate() : null;

                Purchase purchase = new Purchase(
                        rs.getInt("PurchaseID"),
                        rs.getInt("CustomerID"),
                        purchaseDate,
                        rs.getDouble("TotalAmount"),
                        rs.getString("Status"),
                        rs.getInt("GameID"),
                        rs.getInt("Count")
                );
                purchases.add(purchase);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return purchases;
    }

    public void createPurchase(Purchase purchase) {
        String sql = "INSERT INTO Purchase (CustomerID, PurchaseDate, TotalAmount, Status, GameID, Count) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, purchase.getCustomerID());
            if (purchase.getPurchaseDate() != null) {
                pstmt.setDate(2, java.sql.Date.valueOf(purchase.getPurchaseDate()));
            } else {
                pstmt.setNull(2, Types.DATE);
            }
            pstmt.setDouble(3, purchase.getTotalAmount());
            pstmt.setString(4, purchase.getStatus());
            pstmt.setInt(5, purchase.getGameID());
            pstmt.setInt(6, purchase.getCount());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    purchase.setPurchaseID(generatedId);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePurchase(Purchase purchase) {
        String sql = "UPDATE Purchase SET CustomerID=?, PurchaseDate=?, TotalAmount=?, Status=?, GameID=?, Count=? WHERE PurchaseID=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, purchase.getCustomerID());
            if (purchase.getPurchaseDate() != null) {
                pstmt.setDate(2, java.sql.Date.valueOf(purchase.getPurchaseDate()));
            } else {
                pstmt.setNull(2, Types.DATE);
            }
            pstmt.setDouble(3, purchase.getTotalAmount());
            pstmt.setString(4, purchase.getStatus());
            pstmt.setInt(5, purchase.getGameID());
            pstmt.setInt(6, purchase.getCount());
            pstmt.setInt(7, purchase.getPurchaseID());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePurchase(int purchaseID) throws SQLException {
        String sql = "DELETE FROM Purchase WHERE PurchaseID=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, purchaseID);
            pstmt.executeUpdate();
        }
    }
}

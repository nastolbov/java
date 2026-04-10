package org.triangle.base.ui.service;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.triangle.base.Class.Model.Customer;

@Service
public class CustomerService {
    private static final String URL = "jdbc:h2:mem:computer_games";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public List<Customer> getCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM Customer";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                LocalDate regDate = null;
                Date sqlDate = rs.getDate("RegistrationDate");
                if (sqlDate != null) {
                    regDate = sqlDate.toLocalDate();
                }
                Customer customer = new Customer(
                        rs.getInt("CustomerID"),
                        rs.getString("Name"),
                        rs.getString("Email"),
                        rs.getString("PhoneNumber"),
                        rs.getString("Address"),
                        regDate
                );
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }

    public void createCustomer(Customer customer) {
        String sql = "INSERT INTO Customer (CustomerID, Name, Email, PhoneNumber, Address, RegistrationDate) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customer.getCustomerID());
            pstmt.setString(2, customer.getName());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getPhoneNumber());
            pstmt.setString(5, customer.getAddress());

            if (customer.getRegistrationDate() != null) {
                pstmt.setDate(6, java.sql.Date.valueOf(customer.getRegistrationDate()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCustomer(Customer customer) {
        String sql = "UPDATE Customer SET Name=?, Email=?, PhoneNumber=?, Address=?, RegistrationDate=? WHERE CustomerID=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhoneNumber());
            pstmt.setString(4, customer.getAddress());

            if (customer.getRegistrationDate() != null) {
                pstmt.setDate(5, java.sql.Date.valueOf(customer.getRegistrationDate()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }

            pstmt.setInt(6, customer.getCustomerID());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCustomer(int customerID) throws SQLException {
        String sql = "DELETE FROM Customer WHERE CustomerID=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            pstmt.executeUpdate();
        }
    }
}

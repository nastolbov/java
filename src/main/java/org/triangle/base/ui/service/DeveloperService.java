package org.triangle.base.ui.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.triangle.base.Class.Model.Developer;

@Service
public class DeveloperService {
    private static final String URL = "jdbc:h2:mem:computer_games";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public List<Developer> getDevelopers() {
        List<Developer> developers = new ArrayList<>();
        String query = "SELECT DeveloperID, Name, ContactName, PhoneNumber, Email, Address FROM Developer";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Developer developer = new Developer(
                        rs.getInt("DeveloperID"),
                        rs.getString("Name"),
                        rs.getString("ContactName"),
                        rs.getString("PhoneNumber"),
                        rs.getString("Email"),
                        rs.getString("Address")
                );
                developers.add(developer);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return developers;
    }

    public void createDeveloper(Developer developer) {
        String insertSql = "INSERT INTO Developer (Name, ContactName, PhoneNumber, Email, Address) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, developer.getName());
            ps.setString(2, developer.getContactName());
            ps.setString(3, developer.getPhoneNumber());
            ps.setString(4, developer.getEmail());
            ps.setString(5, developer.getAddress());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    developer.setDeveloperID(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateDeveloper(Developer developer) {
        String updateSql = "UPDATE Developer SET Name=?, ContactName=?, PhoneNumber=?, Email=?, Address=? WHERE DeveloperID=?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(updateSql)) {

            ps.setString(1, developer.getName());
            ps.setString(2, developer.getContactName());
            ps.setString(3, developer.getPhoneNumber());
            ps.setString(4, developer.getEmail());
            ps.setString(5, developer.getAddress());
            ps.setInt(6, developer.getDeveloperID());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteDeveloper(int developerID) throws SQLException {
        String deleteSql = "DELETE FROM Developer WHERE DeveloperID=?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(deleteSql)) {

            ps.setInt(1, developerID);
            ps.executeUpdate();
        }
    }
}

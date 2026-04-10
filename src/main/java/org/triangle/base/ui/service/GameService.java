package org.triangle.base.ui.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.triangle.base.Class.Model.Game;

@Service
public class GameService {
    private static final String URL = "jdbc:h2:mem:computer_games";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public List<Game> getGames() {
        List<Game> games = new ArrayList<>();
        String query = "SELECT * FROM Game";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Game g = new Game(
                        rs.getInt("GameID"),
                        rs.getString("Title"),
                        rs.getString("Description"),
                        rs.getDouble("Price"),
                        rs.getInt("StockQuantity"),
                        rs.getInt("GenreID"),
                        rs.getInt("DeveloperID")
                );
                games.add(g);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return games;
    }

    public void createGame(Game game) throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        String insertSQL = """
        INSERT INTO Game (GameID, Title, Description, Price, StockQuantity, GenreID, DeveloperID)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            stmt.setInt(1, game.getGameID());
            stmt.setString(2, game.getTitle());
            stmt.setString(3, game.getDescription());
            stmt.setDouble(4, game.getPrice());
            stmt.setInt(5, game.getStockQuantity());
            stmt.setInt(6, game.getGenreID());
            stmt.setInt(7, game.getDeveloperID());
            stmt.executeUpdate();
        }
    }

    public void updateGame(Game game) throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        String updateSQL = """
                UPDATE Game SET
                    GameID = ?,
                    Title = ?,
                    Description = ?,
                    Price = ?,
                    StockQuantity = ?,
                    GenreID = ?,
                    DeveloperID = ?
                WHERE GameID = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
            stmt.setInt(1, game.getGameID());
            stmt.setString(2, game.getTitle());
            stmt.setString(3, game.getDescription());
            stmt.setDouble(4, game.getPrice());
            stmt.setInt(5, game.getStockQuantity());
            stmt.setInt(6, game.getGenreID());
            stmt.setInt(7, game.getDeveloperID());
            stmt.setInt(8, game.getGameID());
            stmt.executeUpdate();
        }
    }

    public void deleteGame(int gameId) throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        String deleteSQL = "DELETE FROM Game WHERE GameID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
            stmt.setInt(1, gameId);
            stmt.executeUpdate();
        }
    }
}

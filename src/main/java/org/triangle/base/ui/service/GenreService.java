package org.triangle.base.ui.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.triangle.base.Class.Model.Genre;

@Service
public class GenreService {
    private static final String URL = "jdbc:h2:mem:computer_games";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public List<Genre> getGenres() {
        List<Genre> genres = new ArrayList<>();
        String query = "SELECT GenreID, Name, Description FROM Genre";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Genre genre = new Genre(
                        rs.getInt("GenreID"),
                        rs.getString("Name"),
                        rs.getString("Description")
                );
                genres.add(genre);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return genres;
    }

    public void createGenre(Genre genre) {
        String sql = "INSERT INTO Genre (Name, Description) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, genre.getName());
            pstmt.setString(2, genre.getDescription());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    genre.setGenreID(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateGenre(Genre genre) {
        String sql = "UPDATE Genre SET Name = ?, Description = ? WHERE GenreID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, genre.getName());
            pstmt.setString(2, genre.getDescription());
            pstmt.setInt(3, genre.getGenreID());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteGenre(int genreId) throws SQLException {
        String sql = "DELETE FROM Genre WHERE GenreID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, genreId);
            pstmt.executeUpdate();
        }
    }
}

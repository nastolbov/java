package org.triangle.base.Class.Model;

public class Genre {
    private int genreID;
    private String name;
    private String description;

    public Genre(int genreID, String name, String description) {
        this.genreID = genreID;
        this.name = name;
        this.description = description;
    }

    public int getGenreID() {
        return genreID;
    }

    public void setGenreID(int genreID) {
        this.genreID = genreID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

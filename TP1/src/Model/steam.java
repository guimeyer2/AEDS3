package Model;

import java.time.LocalDate;
import java.util.ArrayList;

public class steam {
    private int appid;
    private String name;
    private LocalDate release_date;
    private ArrayList<String> platforms;
    private String genres;

    public steam(int appid, String name, LocalDate release_date, ArrayList<String> platforms, String genres) {
        this.appid = appid;
        this.name = name;
        this.release_date = release_date;
        this.platforms = platforms;
        this.genres = genres;
    }

    public steam() {
        this.appid = -1;
        this.name = "";
        this.release_date = null;
        this.platforms = new ArrayList<>();
        this.genres = "";
    }

    public int getAppid() {
        return appid;
    }

    public void setAppid(int appid) {
        this.appid = appid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getReleaseDate() {
        return release_date;
    }

    public void setReleaseDate(LocalDate release_date) {
        this.release_date = release_date;
    }

    public ArrayList<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(ArrayList<String> platforms) {
        this.platforms = platforms;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }
}

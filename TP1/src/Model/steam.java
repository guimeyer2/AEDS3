package Model;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
// Tirar as colunas que nao estao sendo utilizadas e criar o objeto pra ser utilizado no Actions.
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

    public int getAppid() { return appid; }
    public void setAppid(int appid) { this.appid = appid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getReleaseDate() { return release_date; }
    public void setReleaseDate(LocalDate release_date) { this.release_date = release_date; }

    public ArrayList<String> getPlatforms() { return platforms; }
    public void setPlatforms(ArrayList<String> platforms) { this.platforms = platforms; }

    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }

    public byte[] toByteArray() {
        try {
            ByteArrayOutputStream by = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(by);
            
            dos.writeInt(appid);
            dos.writeUTF(name);
            dos.writeUTF(release_date != null ? release_date.toString() : "");
            dos.writeInt(platforms.size());
            for (String platform : platforms) {
                dos.writeUTF(platform);
            }
            dos.writeUTF(genres);
            
            dos.close();
            return by.toByteArray();
        } catch (IOException e) {
            System.out.println("Erro ao serializar: " + e.getMessage());
            return null;
        }
    }

    public void fromByteArray(byte[] by) {
        try {
            ByteArrayInputStream vet = new ByteArrayInputStream(by);
            DataInputStream dis = new DataInputStream(vet);
            
            this.appid = dis.readInt();
            this.name = dis.readUTF();
            String dateString = dis.readUTF();
            this.release_date = dateString.isEmpty() ? null : LocalDate.parse(dateString);
            
            int platformSize = dis.readInt();
            this.platforms = new ArrayList<>();
            for (int i = 0; i < platformSize; i++) {
                this.platforms.add(dis.readUTF());
            }
            
            this.genres = dis.readUTF();
            
            dis.close();
        } catch (IOException e) {
            System.out.println("Erro ao desserializar: " + e.getMessage());
        }
    }

    public void printGame() {
        System.out.println("AppID: " + this.appid);
        System.out.println("Nome: " + this.name);
        System.out.println("Data de Lançamento: " + (this.release_date != null ? this.release_date : "N/A"));
        System.out.println("Plataformas: " + String.join(", ", this.platforms));
        System.out.println("Gêneros: " + this.genres);
    }
}

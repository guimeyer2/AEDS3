package Model;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class steam {
    private int appid; // inteiro
    private String name; // string de tamanho variado
    private LocalDate release_date; // data
    private ArrayList<String> platforms; // lista com separador
    private String genres; // string de tamanho variado
    private String launchBefore2010; // string de tamanho fixo (SIM ou NAO)

    public steam(int appid, String name, LocalDate release_date, ArrayList<String> platforms, String genres, String launchBefore2010) {
        this.appid = appid;
        this.name = name;
        this.release_date = release_date;
        this.platforms = platforms;
        this.genres = genres;
        this.launchBefore2010 = launchBefore2010; // Valor lido diretamente do CSV
    }

    public steam() {
        this.appid = -1;
        this.name = "";
        this.release_date = null;
        this.platforms = new ArrayList<>();
        this.genres = "";
        this.launchBefore2010 = "NAO"; 
    }

    // Getters e Setters
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

    public String getLaunchBefore2010() { return launchBefore2010; }
    public void setLaunchBefore2010(String launchBefore2010) { this.launchBefore2010 = launchBefore2010; }

    // Serialização para byte array
    public byte[] toByteArray() {
        try {
            ByteArrayOutputStream by = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(by);

            dos.writeInt(appid); // ID
            dos.writeUTF(name); // Nome
            dos.writeUTF(release_date != null ? release_date.toString() : ""); // Data de lançamento
            dos.writeInt(platforms.size()); // Tamanho da lista de plataformas
            for (String platform : platforms) {
                dos.writeUTF(platform); // Plataformas
            }
            dos.writeUTF(genres); // Gêneros
            dos.writeUTF(launchBefore2010); // Lançado antes de 2010

            dos.close();
            return by.toByteArray();
        } catch (IOException e) {
            System.out.println("Erro ao serializar: " + e.getMessage());
            return null;
        }
    }

    // Desserialização de byte array
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
            this.launchBefore2010 = dis.readUTF(); 

            dis.close();
        } catch (IOException e) {
            System.out.println("Erro ao desserializar: " + e.getMessage());
        }
    }

    // Método para exibir os dados do jogo
    @Override
public String toString() {
    return "AppID: " + appid + "\n" +
           "Nome: " + name + "\n" +
           "Data de Lançamento: " + (release_date != null ? release_date : "N/A") + "\n" +
           "Plataformas: " + String.join(", ", platforms) + "\n" +
           "Gêneros: " + genres + "\n" +
           "Lançado antes de 2010: " + launchBefore2010;
}

}
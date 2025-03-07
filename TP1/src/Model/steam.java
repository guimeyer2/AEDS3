package Model;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
// Tirar as colunas que nao estao sendo utilizadas e criar o objeto pra ser utilizado no Actions.
public class steam {
    private int appid;
    private String name;
    private LocalDate release_date;
    private boolean english;
    private String developer;
    private String publisher;
    private ArrayList<String> plataforms;
    private int required_age;
    private String categories;
    private String genres;
    private String steamspy_tags;
    private String achievements;
    private int positive_ratings;
    private int negative_ratings;
    private int average_playtime;
    private int median_playtime;
    private String owners;
    private Double price;

    public steam(int appid, String name, LocalDate release_date, boolean english, String developer, String publisher,
            ArrayList <String> plataforms,int required_age, String categories, String genres, String steamspy_tags,String achievements,
            int positive_ratings,int negative_ratings,int average_playtime,int median_playtime,String owners,Double price) {

        this.appid = appid;
        this.name = name;
        this.release_date = release_date;
        this.english = english;
        this.developer = developer;
        this.publisher = publisher;
        this.plataforms = plataforms;
        this.required_age = required_age;
        this.categories = categories;
        this.genres = genres;
        this.steamspy_tags = steamspy_tags;
        this.achievements = achievements;
        this.positive_ratings = positive_ratings;
        this.negative_ratings = negative_ratings;
        this.average_playtime = average_playtime;  
        this.median_playtime = median_playtime;
        this.owners = owners;
        this.price = price;
    }

    public steam() {

        this.appid = -1;
        this.name = "";
        this.release_date = null;
        this.english = false;
        this.developer = "";
        this.publisher = "";
        this.plataforms = null;
        this.required_age = -1;
        this.categories = "";
        this.genres = "";
        this.steamspy_tags = "";
        this.achievements = "";
        this.positive_ratings = -1;
        this.negative_ratings = -1;
        this.average_playtime = -1; 
        this.median_playtime = -1;
        this.owners = "";
        this.price = 0.0;   
    }

    public byte[] toByteArray() throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeInt(appid);
        dataOutputStream.writeUTF(name);
        dataOutputStream.writeUTF("");
        dataOutputStream.writeBoolean(english);
        dataOutputStream.writeUTF(developer);
        dataOutputStream.writeUTF(publisher);
        dataOutputStream.writeUTF("");
        dataOutputStream.writeInt(required_age);
        dataOutputStream.writeUTF(categories);
        dataOutputStream.writeUTF(genres);
        dataOutputStream.writeUTF(steamspy_tags);
        dataOutputStream.writeUTF(achievements);
        dataOutputStream.writeInt(positive_ratings);
        dataOutputStream.writeInt(negative_ratings);
        dataOutputStream.writeInt(average_playtime);
        dataOutputStream.writeInt(median_playtime);
        dataOutputStream.writeUTF(owners);
        dataOutputStream.writeDouble(price);

        return byteArrayOutputStream.toByteArray();
    }

}
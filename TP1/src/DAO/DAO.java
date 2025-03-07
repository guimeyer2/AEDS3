package DAO;
// Nao acho necessário
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;

import Model.steam;

public class DAO {
    public static void insert(String[] row, FileOutputStream fileOutputStream) {

        try {

            // Format

            String name = row[0];
            Double netWorth = Double.parseDouble(row[1]); // String -> Double
            String country = row[2];
            String[] sourceArray = row[3].split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            ArrayList<String> source = new ArrayList<String>();
            for (String i : sourceArray) {
                source.add(i);
            }
            int rank = Integer.parseInt(row[4]); // String -> Int
            Double age = Double.parseDouble(row[5]); // String(Float) -> Double !!!
            String residence = row[6];
            String citizenship = row[7];
            String status = row[8];
            Double children = Double.parseDouble(row[9]); // String(Float) -> Double !!!
            String education = row[10];
            Boolean self_made = Boolean.parseBoolean(row[11]); // String -> Boolean
            LocalDate birthdate = LocalDate.parse(row[12]); // String -> LocalDate !!!

            // Jeff Bezos,177.0,United States,Amazon,1,57.0,"Seattle, Washington",United
            // States,In Relationship,4.0,"Bachelor of Arts/Science, Princeton
            // University",True,1968-01-01

            steam steam = new steam(rank, name, birthdate, false, education, name, source, rank, country, residence, citizenship, status, rank, rank, rank, rank, education, children);

            DataOutputStream dataOutputStream;

            byte[] bt;

            // Write

            dataOutputStream = new DataOutputStream(fileOutputStream);

            bt = steam.toByteArray();
            dataOutputStream.writeInt(8); // Byte para guardar tamanho do Objeto
            dataOutputStream.writeInt(bt.length); // Byte para guardar tamanho do Objeto
            dataOutputStream.write(bt); // Insere objeto

        } catch (Exception e) {
            System.err.println("Erro: " + e);
        }

    }

    public static void getAll() {

    }

}
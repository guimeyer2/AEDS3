package controller;


import java.io.*;
import java.nio.file.*;

public class Formatar {
    public static void main(String[] args) {
        String inputFilePath = "steam.csv";  
        String outputFilePath = "lib/steam2.csv"; 

        try (BufferedReader br = Files.newBufferedReader(Paths.get(inputFilePath));
             BufferedWriter bw = Files.newBufferedWriter(Paths.get(outputFilePath))) {
            
            String header = br.readLine(); 
           

            String[] columns = header.split(",");
            int indexID = -1, indexName = -1, indexDate = -1, indexGenre = -1, indexPlatform = -1;
            
            for (int i = 0; i < columns.length; i++) {
                String col = columns[i].trim().toLowerCase();
                if (col.equals("id")) indexID = i;
                else if (col.equals("name")) indexName = i;
                else if (col.equals("release date")) indexDate = i;
                else if (col.equals("genres")) indexGenre = i;
                else if (col.equals("platforms")) indexPlatform = i;
            }

          
            if (indexID == -1 || indexName == -1 || indexDate == -1 || indexGenre == -1 || indexPlatform == -1) {
                System.out.println("Erro: Algumas colunas não foram encontradas no CSV.");
                return;
            }

            // Escreve o cabeçalho formatado no novo arquivo
            bw.write("ID,Name,Release Date,Genres,Platforms\n");

            // Processa cada linha do CSV
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Divide os valores
                if (values.length < Math.max(indexID, indexPlatform) + 1) continue; // Verifica se a linha tem colunas suficientes

                // Extrai os dados formatados
                String id = values[indexID].trim();
                String name = values[indexName].trim();
                String releaseDate = values[indexDate].trim();
                String genres = values[indexGenre].trim();
                String platforms = values[indexPlatform].trim();

                // Escreve no novo CSV
                bw.write(String.join(",", id, name, releaseDate, genres, platforms) + "\n");
            }

            System.out.println("Novo CSV formatado salvo como: " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package controller;

import java.io.*;
import java.nio.file.*;
import java.util.regex.*;

public class Formatar {
    public static void main(String[] args) {
        String inputFilePath = "TP1/src/steam.csv";  // Caminho do arquivo original
        String outputFilePath = "TP1/src/steam2.csv"; // Caminho do arquivo formatado

        try (BufferedReader br = Files.newBufferedReader(Paths.get(inputFilePath));
             BufferedWriter bw = Files.newBufferedWriter(Paths.get(outputFilePath))) {

            String header = br.readLine();  // Lê o cabeçalho

            String[] columns = header.split(",");
            System.out.println("Colunas encontradas no CSV:");
            for (String col : columns) {
                System.out.println("'" + col + "'");
            }

            // Identificar os índices das colunas
            int indexID = -1, indexName = -1, indexDate = -1, indexGenre = -1, indexPlatform = -1;
            for (int i = 0; i < columns.length; i++) {
                String col = columns[i].trim().toLowerCase();
                if (col.equals("appid")) indexID = i;
                else if (col.equals("name")) indexName = i;
                else if (col.equals("release_date")) indexDate = i;
                else if (col.equals("genres")) indexGenre = i;
                else if (col.equals("platforms")) indexPlatform = i;
            }

            // Verifica se todas as colunas necessárias foram encontradas
            if (indexID == -1 || indexName == -1 || indexDate == -1 || indexGenre == -1 || indexPlatform == -1) {
                System.out.println("Erro: Algumas colunas não foram encontradas no CSV.");
                return;
            }

            // Escreve o cabeçalho formatado no novo arquivo, adicionando a nova coluna "launchBefore2010"
            bw.write("ID,Name,Release Date,Genres,Platforms,LaunchBefore2010\n");

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

                // Verifica se o jogo foi lançado antes de 2010 e adiciona a coluna "LaunchBefore2010"
                String launchBefore2010 = isBefore2010(releaseDate) ? "SIM" : "NAO";

                // Escreve a linha no novo CSV
                bw.write(String.join(",", id, name, releaseDate, genres, platforms, launchBefore2010) + "\n");
            }

            System.out.println("Novo CSV formatado salvo como: " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para verificar se o jogo foi lançado antes de 2010
    public static boolean isBefore2010(String releaseDate) {
        try {
            releaseDate = releaseDate.replaceAll("\"", "").trim();  // Remover aspas e espaços extras
            System.out.println("Verificando data: " + releaseDate); // Debug para ver se a data está correta
    
            String datePattern = "^\\d{4}-\\d{2}-\\d{2}$";
            if (!Pattern.matches(datePattern, releaseDate)) {
                System.out.println("Ignorando valor não relacionado a data: " + releaseDate);
                return false;  // Retorna "NAO" se não for uma data válida
            }
    
            int year = Integer.parseInt(releaseDate.split("-")[0]);  // Extrai o ano
            return year < 2010;  // Compara o ano
        } catch (Exception e) {
            System.out.println("Erro ao analisar a data: " + releaseDate);
            return false;
        }
    }
}    
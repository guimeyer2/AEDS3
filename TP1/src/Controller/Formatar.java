package controller;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Formatar {
    public static void main(String[] args) {
        // Caminho dos arquivos de entrada e saída
        String inputFilePath = "TP1/src/steam.csv";
        String outputFilePath = "TP1/src/steam2.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            // Lê o cabeçalho do arquivo CSV original
            String header = br.readLine(); 
            if (header == null) { // Verifica se o arquivo está vazio
                System.out.println("Erro: O arquivo está vazio.");
                return;
            }

            // Escreve o novo cabeçalho no arquivo formatado
            bw.write("ID,Name,Release Date,Genres,Platforms\n");

            String line;
            while ((line = br.readLine()) != null) { // Lê cada linha do arquivo CSV
                List<String> values = parseCSVLine(line); // Processa a linha

                if (values.size() < 5) { // Verifica se a linha contém todas as colunas necessárias
                    continue;
                }

                // Extrai os campos necessários e remove espaços extras
                String id = values.get(0).trim();
                String name = values.get(1).trim();
                String releaseDate = values.get(2).trim();
                String genres = values.get(3).trim();
                String platforms = values.get(4).trim();

                // Escreve a linha formatada no novo arquivo
                bw.write(String.join(",", id, name, releaseDate, genres, platforms) + "\n");
            }

            System.out.println("Novo CSV formatado salvo como: " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace(); // Captura e exibe erros de leitura/escrita
        }
    }

    // Método para processar corretamente linhas do CSV, respeitando campos entre aspas
    private static List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]*)\"|([^,]+)").matcher(line);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                result.add(matcher.group(1)); // Campo dentro de aspas
            } else {
                result.add(matcher.group(2)); // Campo sem aspas
            }
        }
        return result;
    }
}
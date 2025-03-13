package controller;

import Model.steam;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class Actions {

    private long lastPos;
    private int maxId;
    private int gamesCount;
    RandomAccessFile file;

    public void openFile() throws IOException {
        File dbFile = new File("TP1/src/steam.db");
    
        
        if (!dbFile.exists()) {
            dbFile.getParentFile().mkdirs(); 
            dbFile.createNewFile();
            System.out.println("Arquivo steam.db criado!");
        }
    
        file = new RandomAccessFile(dbFile, "rw");
        if (file.length() == 0) {
            file.writeLong(8); 
        }
    
        lastPos = file.readLong();
        maxId = 0;
        gamesCount = maxId;
    }
    

    public void closeFile() throws IOException {
        try {
            file.close();
        } catch (Exception e) {
            System.err.println("Erro ao fechar arquivo .db: " + e);
        }
    }

    public void loadData() {
        try (BufferedReader csv = new BufferedReader(new FileReader("TP1/src/steam2.csv"));
             RandomAccessFile write = new RandomAccessFile("TP1/src/steam.db", "rw")) {
    
            // Pular o cabeçalho
            csv.readLine();
    
            // Escrever o cabeçalho do arquivo binário (último ID e posição do último registro)
            write.writeInt(0); // Último ID inicializado como 0
            write.writeLong(12); // Posição inicial do primeiro registro (após o cabeçalho)
    
            System.out.println("Carregando dados para o arquivo...");
    
            String str;
            int lastId = 0; // Para armazenar o último ID utilizado
    
            while ((str = csv.readLine()) != null) {
                if (str.trim().isEmpty()) continue; // Ignorar linhas em branco
    
                String[] vet = str.split(",");
    
                // Verificar se a linha tem o número esperado de colunas
                if (vet.length < 6) {
                    System.err.println("Linha mal formatada (menos de 6 colunas): " + str);
                    continue;
                }
    
                // Tentar parse da data com tratamento de exceção
                LocalDate releaseDate = null;
                try {
                    releaseDate = LocalDate.parse(vet[2]); // A data deve estar no formato AAAA-MM-DD
                } catch (Exception e) {
                    System.err.println("Erro ao parsear data: " + vet[2] + " - " + e.getMessage());
                    continue; // Pular essa linha e continuar com a próxima
                }
    
                // Processar as plataformas (separadas por ";")
                ArrayList<String> platforms = new ArrayList<>();
                String[] platformList = vet[4].split(";");
                for (String platform : platformList) {
                    platforms.add(platform.trim());
                }
    
                // Ler o valor de LaunchBefore2010 diretamente do CSV
                String launchBefore2010 = vet[5].trim();
    
                // Criar o objeto 'steam' com os dados da linha
                steam tmp = new steam(
                    Integer.parseInt(vet[0]), // ID
                    vet[1], // Nome
                    releaseDate, // Data de lançamento
                    platforms, // Plataformas
                    vet[3], // Gêneros
                    launchBefore2010 // Lançado antes de 2010
                );
    
                // Converter o objeto 'steam' para um array de bytes
                byte[] aux = tmp.toByteArray();
    
                // Escrever o tamanho do registro e o vetor de bytes no arquivo binário
                write.writeInt(aux.length); // Tamanho do registro
                write.write(aux); // Vetor de bytes
    
                // Atualizar o último ID
                lastId = tmp.getAppid();
            }
    
            // Atualizar o cabeçalho do arquivo binário com o último ID e a posição do último registro
            write.seek(0);
            write.writeInt(lastId); // Último ID utilizado
            write.writeLong(write.getFilePointer()); // Posição do último registro
    
            System.out.println("Dados carregados com sucesso!");
    
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados: " + e);
        }
    }

    public boolean isGameValid(byte arr[], int id) {
        try (ByteArrayInputStream by = new ByteArrayInputStream(arr);
             DataInputStream dis = new DataInputStream(by)) {
            return dis.readInt() == id;
        } catch (Exception e) {
            System.err.println("Erro na checagem de validade do Game: " + e);
            return false;
        }
    }

    public boolean createGame(steam tmp) {
        try {
            gamesCount++;
            maxId++;
            file.seek(lastPos);

            byte[] aux = tmp.toByteArray();
            file.writeInt(aux.length);
            file.write(aux);
            lastPos += aux.length;
            return true;
        } catch (IOException e) {
            System.err.println("Erro na função create: " + e);
            return false;
        }
    }

    public steam readGame(int searchId) throws IOException {
        steam aux = new steam();
        long pos = 12; // Pula o cabeçalho (4 bytes para o último ID + 8 bytes para a posição do último registro)
    
        try {
            file.seek(pos);
    
            for (int i = 0; i < gamesCount; i++) {
                int tam = file.readInt(); // Lê o tamanho do registro
                byte[] tempVet = new byte[tam];
                file.read(tempVet); // Lê o vetor de bytes do registro
    
                // Verifica se o registro é válido (lápide == 0)
                if (tempVet[0] == 0) {
                    aux.fromByteArray(tempVet); // Desserializa o registro
                    if (aux.getAppid() == searchId) { // Verifica se o ID corresponde
                        return aux;
                    }
                }
    
                pos += 4 + tam; // Avança para o próximo registro (4 bytes para o tamanho + tamanho do registro)
            }
        } catch (Exception e) {
            System.err.println("Erro na função read: " + e);
        }
        return null; // Retorna null se o jogo não for encontrado
    }

    public boolean updateGame(int id, steam insert) {
        long pos = 8;

        try {
            file.seek(pos);
            for (int i = 0; i < gamesCount; i++) {
                int tam = file.readInt();
                byte[] arr = new byte[tam];
                file.read(arr);

                if (isGameValid(arr, id)) {
                    if (tam >= insert.toByteArray().length) {
                        file.seek(pos + 4);
                        file.write(insert.toByteArray());
                        return true;
                    } else {
                        createGame(insert);
                        gamesCount++;
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro na função update: " + e);
        }
        return false;
    }

    public steam deleteGame(int id) {
        steam aux = new steam();
        long pos = 8;

        try {
            file.seek(pos);

            for (int i = 0; i < maxId; i++) {
                int tam = file.readInt();
                byte[] temp = new byte[tam];
                file.read(temp);

                if (isGameValid(temp, id)) {
                    file.seek(pos + 4);
                    aux.fromByteArray(temp);
                    aux.setAppid(-1);
                    file.write(aux.toByteArray());
                    return aux;
                }
            }
        } catch (Exception e) {
            System.err.println("Erro na função delete: " + e);
        }
        return null;
    }
}

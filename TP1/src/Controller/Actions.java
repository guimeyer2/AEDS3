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
        File sortedDbFile = new File("TP1/src/steam_sorted.db");
        File dbFile = new File("TP1/src/steam.db");
    
        // Verifica se o arquivo ordenado já foi criado, se sim, usa ele
        if (sortedDbFile.exists()) {
            file = new RandomAccessFile(sortedDbFile, "rw");
            System.out.println("Usando o arquivo ordenado: steam_sorted.db");
        } else {
            file = new RandomAccessFile(dbFile, "rw");
            System.out.println("Usando o arquivo original: steam.db");
        }
    
        if (file.length() == 0) {
            file.writeInt(0);
            file.writeLong(12);
            lastPos = 12;
        } else {
            file.seek(0);
            maxId = file.readInt();
            lastPos = file.readLong();
        }
    
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

            csv.readLine();
            write.writeInt(0);
            write.writeLong(12);

            System.out.println("Carregando dados para o arquivo...");

            String str;
            int lastId = 0;

            while ((str = csv.readLine()) != null) {
                if (str.trim().isEmpty()) continue;

                String[] vet = str.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (vet.length < 6) {
                    continue;
                }

                if (vet[0].isEmpty() || vet[1].isEmpty() || vet[2].isEmpty()) {
                    System.err.println("Linha com dados faltando: " + str);
                    continue;
                }

                int appid;
                try {
                    appid = Integer.parseInt(vet[0]);
                } catch (NumberFormatException e) {
                    System.err.println("Erro ao converter AppID para número: " + vet[0]);
                    continue;
                }

                LocalDate releaseDate = null;
                try {
                    releaseDate = LocalDate.parse(vet[2]);
                } catch (Exception e) {
                    System.err.println("Erro ao parsear data: " + vet[2]);
                    continue;
                }

                ArrayList<String> platforms = new ArrayList<>();
                if (!vet[4].isEmpty()) {
                    for (String platform : vet[4].split(";")) {
                        platforms.add(platform.trim());
                    }
                }

                steam tmp = new steam(appid, vet[1], releaseDate, platforms, vet[3], vet[5].trim());
                byte[] aux = tmp.toByteArray();

                write.writeByte(0);
                write.writeInt(aux.length);
                write.write(aux);
                lastId = tmp.getAppid();
            }

            write.seek(0);
            write.writeInt(lastId);
            write.writeLong(write.getFilePointer());
            System.out.println("Dados carregados com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao carregar dados: " + e);
        }
    }


    public boolean isGameValid(byte[] arr, int id) {
        try {
            steam temp = new steam();
            temp.fromByteArray(arr);
            return temp.getAppid() == id;
        } catch (Exception e) {
            System.err.println("Erro na validação do jogo: " + e);
            return false;
        }
    }
    
    public steam readGame(int searchId) throws IOException {
        long pos = 12;
        file.seek(pos);
    
        try {
            while (file.getFilePointer() < file.length()) {
                long regPos = file.getFilePointer();
                byte tombstone = file.readByte();
                
                if (file.getFilePointer() + 4 > file.length()) {
                    System.err.println("❌ Erro: Tentando ler um tamanho inválido! Registro pode estar corrompido.");
                    return null;
                }
    
                int tam = file.readInt();
    
               
    
                if (tam <= 0 || tam > file.length() - file.getFilePointer()) {
                    System.err.println("❌ Erro: tamanho do registro inválido! Algo está corrompido.");
                    return null;
                }
    
                byte[] tempVet = new byte[tam];
                file.read(tempVet);
    
                if (tombstone == 0 && isGameValid(tempVet, searchId)) {
                    steam game = new steam();
                    game.fromByteArray(tempVet);
                    return game;
                }
    
                pos = file.getFilePointer(); // Atualiza a posição corretamente
                file.seek(pos);
            }
        } catch (Exception e) {
            System.err.println("Erro na função readGame: " + e.getMessage());
        }
    
        return null;
    }
    
    

    
    
    
    public boolean updateGame(int id, steam newGame) {
        long pos = 12;
    
        try {
            file.seek(pos);
    
            while (file.getFilePointer() < file.length()) {
                long regPos = file.getFilePointer();
                byte tombstone = file.readByte();
                int tam = file.readInt();
                byte[] arr = new byte[tam];
                file.read(arr);
    
                if (tombstone == 0 && isGameValid(arr, id)) {
                    byte[] newGameBytes = newGame.toByteArray();
    
                    if (newGameBytes.length <= tam) {
                        file.seek(regPos + 5);
                        file.write(newGameBytes);
                        return true;
                    } else {
                        file.seek(regPos);
                        file.writeByte(1);
                        return createGame(newGame);
                    }
                }
    
                pos += 5 + tam;
            }
        } catch (Exception e) {
            System.err.println("Erro na função updateGame: " + e);
        }
        return false;
    }

    public boolean createGame(steam tmp) {
        try {
            if (readGame(tmp.getAppid()) != null) {
                System.err.println("Erro: Já existe um jogo com o AppID " + tmp.getAppid());
                return false;
            }
    
            byte[] aux = tmp.toByteArray();
    
            // Ir para o final do arquivo
            file.seek(file.length());
    
            long newPos = file.getFilePointer(); // Atualiza para a nova posição correta
    
            file.writeByte(0);
            file.writeInt(aux.length);
            file.write(aux);
    
            lastPos = file.getFilePointer(); // Atualiza a posição final corretamente
    
            if (tmp.getAppid() > maxId) {
                maxId = tmp.getAppid();
            }
    
            // Atualiza os metadados corretamente
            file.seek(0);
            file.writeInt(maxId);
            file.writeLong(lastPos);
            
            return true;
        } catch (IOException e) {
            System.err.println("Erro na função createGame: " + e);
            return false;
        }
    }
    
    
    

    public steam deleteGame(int id) {
        steam aux = new steam();
        long pos = 12;

        try {
            file.seek(pos);

            while (file.getFilePointer() < file.length()) {
                long regPos = file.getFilePointer();
                byte tombstone = file.readByte();
                int tam = file.readInt();
                byte[] temp = new byte[tam];
                file.read(temp);

                if (tombstone == 0 && isGameValid(temp, id)) {
                    file.seek(regPos);
                    file.writeByte(1);
                    aux.fromByteArray(temp);
                    return aux;
                }

                pos += 5 + tam;
            }
        } catch (Exception e) {
            System.err.println("Erro na função deleteGame: " + e);
        }
        return null;
    }
}
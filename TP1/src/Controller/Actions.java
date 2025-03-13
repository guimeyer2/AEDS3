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
            file.writeInt(0); // Último ID
            file.writeLong(12); // Posição do primeiro registro
            lastPos = 12;
        } else {
            file.seek(0);
            maxId = file.readInt(); // Lê o último ID salvo
            lastPos = file.readLong(); // Lê a posição do último registro
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

                String[] vet = str.split(",");
                if (vet.length < 6) {
                    System.err.println("Linha mal formatada: " + str);
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
                for (String platform : vet[4].split(";")) {
                    platforms.add(platform.trim());
                }

                steam tmp = new steam(Integer.parseInt(vet[0]), vet[1], releaseDate, platforms, vet[3], vet[5].trim());
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
    private boolean isGameValid(byte[] arr, int id) {
        try (ByteArrayInputStream by = new ByteArrayInputStream(arr);
             DataInputStream dis = new DataInputStream(by)) {
            return dis.readInt() == id;
        } catch (Exception e) {
            System.err.println("Erro na validação do jogo: " + e);
            return false;
        }
    }
    

    public steam readGame(int searchId) throws IOException {
        long pos = 12; // Posição inicial do primeiro registro
        file.seek(pos);
    
        try {
            while (file.getFilePointer() < file.length()) {
                long regPos = file.getFilePointer(); // Guarda a posição do registro
                
                System.out.println("\nLendo registro na posição: " + regPos);
                
                byte tombstone = file.readByte(); // Lê o tombstone
                System.out.println("Tombstone: " + tombstone);
                
                int tam = file.readInt(); // Lê o tamanho do registro
                System.out.println("Tamanho: " + tam);
    
                if (tam <= 0 || tam > file.length()) {
                    System.out.println("❌ Erro: tamanho do registro inválido! Algo está corrompido.");
                    return null;
                }
    
                byte[] tempVet = new byte[tam]; // Lê os bytes do jogo
                file.read(tempVet);
    
                // Se não estiver deletado (tombstone == 0) e for o jogo certo
                if (tombstone == 0 && isGameValid(tempVet, searchId)) {
                    steam game = new steam();
                    game.fromByteArray(tempVet);
                    System.out.println("🎮 Jogo encontrado! ID: " + game.getAppid());
                    return game;
                }
    
                // Avança para o próximo registro
                pos += 5 + tam;
                file.seek(pos);
            }
        } catch (Exception e) {
            System.err.println("Erro na função readGame: " + e.getMessage());
        }
    
        System.out.println("🚫 Jogo não encontrado.");
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
            file.seek(lastPos);

            byte[] aux = tmp.toByteArray();
            file.writeByte(0);
            file.writeInt(aux.length);
            file.write(aux);

            lastPos = file.getFilePointer();

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
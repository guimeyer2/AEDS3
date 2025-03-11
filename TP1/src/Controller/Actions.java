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
        file = new RandomAccessFile("./TP01/out/steam.db", "rw");
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
        try (RandomAccessFile csv = new RandomAccessFile("./TP01/db/steam.csv", "r");
             RandomAccessFile write = new RandomAccessFile("./TP01/out/steam.db", "rw")) {

            csv.readLine();
            String str;

            write.writeLong(8);

            System.out.println("Carregando dados para o arquivo...");

            while ((str = csv.readLine()) != null) {
                String vet[] = str.split(";");
                ArrayList<String> platforms = new ArrayList<>();
                platforms.add(vet[3]);
                
                steam tmp = new steam(
                    Integer.parseInt(vet[0]),
                    vet[1],
                    LocalDate.parse(vet[2]),
                    platforms,
                    vet[4]
                );

                byte aux[] = tmp.toByteArray();
                write.writeInt(aux.length);
                write.write(aux);
            }

            long last = write.getFilePointer();
            write.seek(0);
            write.writeLong(last);

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
        long pos = 8;

        try {
            file.seek(pos);

            for (int i = 0; i < maxId; i++) {
                int tam = file.readInt();
                byte[] tempVet = new byte[tam];
                file.read(tempVet);

                if (isGameValid(tempVet, searchId)) {
                    aux.fromByteArray(tempVet);
                    return aux;
                }
                pos += tam;
            }
        } catch (Exception e) {
            System.err.println("Erro na função Read: " + e);
        }
        return null;
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

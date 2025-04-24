package controller;

import java.io.*;
import java.util.*;
import java.time.LocalDate;

import Model.steam;
import controller.*;

public final class CRUDJava {

    private static final String arquivo = "data/database/steam.db";

    private CRUDJava() {}

    ///////////////////////////////////////////////// CREATE /////////////////////////////////////////////////

    public static void create(steam game, boolean showMessage) {
        try (RandomAccessFile file = new RandomAccessFile(arquivo, "rw")) {

            game.setAppid(obterProximoId());
            file.seek(0);
            file.writeInt(game.getAppid());

            long endereco = file.length();
            file.seek(endereco);

            byte[] array = game.toByteArray();

            file.writeBoolean(false); // lápide
            file.writeInt(array.length);
            file.write(array);

            if (showMessage) {
                System.out.println("\nJogo adicionado com sucesso! AppID = " + game.getAppid() + "\n");
            }

        } catch (IOException e) {
 
        }
    }

    ///////////////////////////////////////////////// READ /////////////////////////////////////////////////

    public static steam read(int appid) {
        try (RandomAccessFile file = new RandomAccessFile(arquivo, "r")) {
            file.seek(4); // pula o cabeçalho do último ID

            while (file.getFilePointer() < file.length()) {
                long pos = file.getFilePointer();
                boolean lapide = file.readBoolean();
                int tamanho = file.readInt();
                byte[] buffer = new byte[tamanho];
                file.readFully(buffer);

                if (!lapide) {
                    steam game = new steam();
                    game.fromByteArray(buffer);

                    if (game.getAppid() == appid) {
                        return game;
                    }
                }
            }
        } catch (IOException e) {

        }
        return null;
    }

    ///////////////////////////////////////////////// UPDATE /////////////////////////////////////////////////

    public static boolean update(int appid, steam novoGame) {
        try (RandomAccessFile file = new RandomAccessFile(arquivo, "rw")) {
            file.seek(4);

            while (file.getFilePointer() < file.length()) {
                long pos = file.getFilePointer();
                boolean lapide = file.readBoolean();
                int tamanho = file.readInt();
                byte[] buffer = new byte[tamanho];
                file.readFully(buffer);

                if (!lapide) {
                    steam gameAntigo = new steam();
                    gameAntigo.fromByteArray(buffer);

                    if (gameAntigo.getAppid() == appid) {
                        byte[] novoArray = novoGame.toByteArray();

                        if (novoArray.length <= tamanho) {
                            file.seek(pos + 1 + 4); // pula lápide + int
                            file.write(novoArray);

                            if (novoArray.length < tamanho)
                                file.write(new byte[tamanho - novoArray.length]);

                        } else {
                            file.seek(pos);
                            file.writeBoolean(true); // marca como removido

                            long novoEndereco = file.length();
                            file.seek(novoEndereco);
                            file.writeBoolean(false);
                            file.writeInt(novoArray.length);
                            file.write(novoArray);
                        }

                        return true;
                    }
                }
            }
        } catch (IOException e) {
    
        }
        return false;
    }

    ///////////////////////////////////////////////// DELETE /////////////////////////////////////////////////

    public static boolean delete(int appid) {
        try (RandomAccessFile file = new RandomAccessFile(arquivo, "rw")) {
            file.seek(4);

            while (file.getFilePointer() < file.length()) {
                long pos = file.getFilePointer();
                boolean lapide = file.readBoolean();
                int tamanho = file.readInt();
                byte[] buffer = new byte[tamanho];
                file.readFully(buffer);

                if (!lapide) {
                    steam game = new steam();
                    game.fromByteArray(buffer);

                    if (game.getAppid() == appid) {
                        file.seek(pos);
                        file.writeBoolean(true);
                        return true;
                    }
                }
            }
        } catch (IOException e) {
  
        }
        return false;
    }

    ///////////////////////////////////////////////// AUXILIARES /////////////////////////////////////////////////

    private static int obterProximoId() {
        try (RandomAccessFile file = new RandomAccessFile(arquivo, "rw")) {
            if (file.length() == 0) {
                return 0;
            } else {
                file.seek(0);
                return file.readInt() + 1;
            }
        } catch (IOException e) {
           
            return 0;
        }
    }

    public static void reiniciarBD() {
        try (RandomAccessFile file = new RandomAccessFile(arquivo, "rw")) {
            file.setLength(0);
        } catch (IOException e) {
          
        }
    }
}

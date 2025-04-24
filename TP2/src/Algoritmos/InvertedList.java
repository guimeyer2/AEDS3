package Algoritmos;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import Model.steam;

public class InvertedList {

    private static final String arquivo = "steam/invertedGenero.db";
    public static RandomAccessFile file;

    public InvertedList() {
        try {
            file = new RandomAccessFile(arquivo, "rw");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InvertedList(int limpar) {
        try {
            file = new RandomAccessFile(arquivo, "rw");
            file.setLength(0); // Limpa o arquivo
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void inserir(steam jogo) {
        try {
            file.seek(0);

            if (file.length() != 0) {
                String genero;
                boolean adicionou = false;
                while (file.getFilePointer() < file.length()) {
                    genero = file.readUTF();
                    int id = file.readInt();
                    long prox = file.readLong();

                    if (genero.equalsIgnoreCase(jogo.getGenres())) {
                        long ultimaPosicao = file.getFilePointer() - 8; // posição do ponteiro do próximo

                        // Vai até o último encadeamento
                        while (prox != -1) {
                            file.seek(prox);
                            file.readUTF();
                            file.readInt();
                            ultimaPosicao = file.getFilePointer();
                            prox = file.readLong();
                        }

                        file.seek(ultimaPosicao);
                        file.writeLong(file.length());
                        file.seek(file.length());
                        file.writeUTF(jogo.getGenres());
                        file.writeInt(jogo.getAppid());
                        file.writeLong(-1);
                        adicionou = true;
                        break;
                    } else {
                        file.seek(file.getFilePointer() + 8); // pular próximo
                    }
                }

                if (!adicionou) {
                    file.seek(file.length());
                    file.writeUTF(jogo.getGenres());
                    file.writeInt(jogo.getAppid());
                    file.writeLong(-1);
                }
            } else {
                file.writeUTF(jogo.getGenres());
                file.writeInt(jogo.getAppid());
                file.writeLong(-1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Integer> buscar(String genero) {
        List<Integer> lista = new ArrayList<>();

        try {
            file.seek(0);
            while (file.getFilePointer() < file.length()) {
                String generoArq = file.readUTF();
                int id = file.readInt();
                long prox = file.readLong();

                if (generoArq.equalsIgnoreCase(genero)) {
                    lista.add(id);
                    while (prox != -1) {
                        file.seek(prox);
                        file.readUTF();
                        lista.add(file.readInt());
                        prox = file.readLong();
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    public void lerTudo() throws IOException {
        file.seek(0);
        while (file.getFilePointer() < file.length()) {
            String genero = file.readUTF();
            int id = file.readInt();
            long prox = file.readLong();
            System.out.println(genero + " -- " + id + " -- " + prox);
        }
    }
}

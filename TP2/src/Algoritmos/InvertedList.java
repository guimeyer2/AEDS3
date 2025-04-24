package Algoritmos;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import Model.steam;

public class InvertedList {

    private static final String arquivo = "steam/invertedGenero.db";
    public static RandomAccessFile file;

    // Construtor para abrir o arquivo sem limpar
    public InvertedList() {
        try {
            file = new RandomAccessFile(arquivo, "rw");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Construtor para abrir o arquivo e limpar o conteúdo
    public InvertedList(int limpar) {
        try {
            file = new RandomAccessFile(arquivo, "rw");
            file.setLength(0); // Limpa o arquivo
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para inserir um jogo no arquivo invertido
    public void inserir(steam jogo) {
        try {
            file.seek(0); // Coloca o ponteiro no início do arquivo

            if (file.length() != 0) {
                String genero;
                boolean adicionou = false;
                while (file.getFilePointer() < file.length()) {
                    genero = file.readUTF();
                    int id = file.readInt();
                    long prox = file.readLong();

                    if (genero.equalsIgnoreCase(jogo.getGenres())) {
                        long ultimaPosicao = file.getFilePointer() - 8; // Última posição antes do ponteiro do próximo

                        // Vai até o último encadeamento do gênero
                        while (prox != -1) {
                            file.seek(prox);
                            file.readUTF();
                            file.readInt();
                            ultimaPosicao = file.getFilePointer();
                            prox = file.readLong();
                        }

                        // Conecta o novo jogo ao final da lista de encadeamento
                        file.seek(ultimaPosicao);
                        file.writeLong(file.length()); // aponta para o próximo
                        file.seek(file.length());
                        file.writeUTF(jogo.getGenres());
                        file.writeInt(jogo.getAppid());
                        file.writeLong(-1); // Marca o final da lista
                        adicionou = true;
                        break;
                    } else {
                        file.seek(file.getFilePointer() + 8); // Pula o próximo
                    }
                }

                if (!adicionou) {
                    // Se o gênero não existe, cria uma nova entrada
                    file.seek(file.length());
                    file.writeUTF(jogo.getGenres());
                    file.writeInt(jogo.getAppid());
                    file.writeLong(-1); // Marca o final da lista
                }
            } else {
                // Se o arquivo estiver vazio, cria a primeira entrada
                file.writeUTF(jogo.getGenres());
                file.writeInt(jogo.getAppid());
                file.writeLong(-1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para buscar jogos por gênero
    public List<Integer> buscar(String genero) {
        List<Integer> lista = new ArrayList<>();

        try {
            file.seek(0); // Coloca o ponteiro no início do arquivo
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

    // Método para exibir todos os dados armazenados na lista invertida
    public void lerTudo() throws IOException {
        file.seek(0); // Coloca o ponteiro no início do arquivo
        while (file.getFilePointer() < file.length()) {
            String genero = file.readUTF();
            int id = file.readInt();
            long prox = file.readLong();
            System.out.println(genero + " -- " + id + " -- " + prox);
        }
    }

    // Método que será chamado para executar a busca, por exemplo, ao rodar a lista invertida
    public void run() {
        try {
            // Exemplo: Buscando jogos do gênero "Ação"
            System.out.println("Buscando jogos do gênero 'Ação'...");
            List<Integer> jogos = buscar("Ação");
            System.out.println("Jogos encontrados:");
            for (int id : jogos) {
                System.out.println("AppID: " + id);
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar a busca na lista invertida: " + e.getMessage());
        }
    }
}

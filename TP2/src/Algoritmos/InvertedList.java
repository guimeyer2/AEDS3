package Algoritmos;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import Model.steam;

public class InvertedList {
    private static final String GENERO_FILE = "TP2/src/temp/invertedGenero.db";
    private static final String PLATAFORMA_FILE = "TP2/src/temp/invertedPlataforma.db";
    private RandomAccessFile generoFile;
    private RandomAccessFile plataformaFile;

    public InvertedList() {
        try {
            // Garante que a pasta temp existe
            File tempDir = new File("TP2/src/temp");
            if (!tempDir.exists()) {
                if (!tempDir.mkdirs()) {
                    System.err.println("Falha ao criar diretório temp");
                    return;
                }
            }

            // Inicializa os arquivos
            generoFile = new RandomAccessFile(GENERO_FILE, "rw");
            plataformaFile = new RandomAccessFile(PLATAFORMA_FILE, "rw");
            
            // Verifica se os arquivos foram abertos corretamente
            if (generoFile == null || plataformaFile == null) {
                System.err.println("Falha ao abrir arquivos de índice");
            }
        } catch (IOException e) {
            System.err.println("Erro ao inicializar lista invertida: " + e.getMessage());
        }
    }

    public void construirListas() {
        if (generoFile == null || plataformaFile == null) {
            System.err.println("Arquivos de índice não inicializados");
            return;
        }

        try (RandomAccessFile dbFile = new RandomAccessFile("TP2/src/steam.db", "r")) {
            // Limpa as listas existentes
            generoFile.setLength(0);
            plataformaFile.setLength(0);
            
            if (dbFile.length() > 12) {
                dbFile.seek(12); // Pula cabeçalho
                
                int count = 0;
                while (dbFile.getFilePointer() < dbFile.length()) {
                    try {
                        long currentPos = dbFile.getFilePointer();
                        byte tombstone = dbFile.readByte();
                        int tam = dbFile.readInt();
                        
                        if (tam <= 0 || tam > (dbFile.length() - dbFile.getFilePointer())) {
                            System.err.println("Tamanho de registro inválido: " + tam + " na posição " + currentPos);
                            break;
                        }
                        
                        byte[] data = new byte[tam];
                        dbFile.readFully(data);
                        
                        if (tombstone == 0) {
                            steam jogo = new steam();
                            jogo.fromByteArray(data);
                            
                            // Insere por gênero
                            if (jogo.getGenres() != null) {
                                String[] generos = jogo.getGenres().split(";");
                                for (String genero : generos) {
                                    genero = genero.trim();
                                    if (!genero.isEmpty()) {
                                        inserirGenero(genero, jogo.getAppid());
                                    }
                                }
                            }
                            
                            // Insere por plataforma
                            if (jogo.getPlatforms() != null) {
                                for (String plataforma : jogo.getPlatforms()) {
                                    plataforma = plataforma.trim();
                                    if (!plataforma.isEmpty()) {
                                        inserirPlataforma(plataforma, jogo.getAppid());
                                    }
                                }
                            }
                            count++;
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao processar registro na posição " + dbFile.getFilePointer() + ": " + e.getMessage());
                        }
                }
                System.out.println("Listas invertidas construídas com sucesso! " + count + " jogos indexados.");
            }
        } catch (IOException e) {
            System.err.println("Erro ao construir listas invertidas: " + e.getMessage());
        }
    }

    private void inserirGenero(String genero, int appid) throws IOException {
        try {
            if (generoFile == null) {
                System.err.println("Arquivo de gênero não inicializado");
                return;
            }

            // Escreve no final do arquivo
            long novaPos = generoFile.length();
            generoFile.seek(novaPos);
            generoFile.writeUTF(genero);
            generoFile.writeInt(appid);
            generoFile.writeLong(-1); // Inicialmente sem próximo

            // Procura por entradas existentes
            generoFile.seek(0);
            long posUltimo = -1;
            
            while (generoFile.getFilePointer() < novaPos) {
                long posAtual = generoFile.getFilePointer();
                String generoAtual = generoFile.readUTF();
                generoFile.readInt(); // Pula o ID
                long proxPos = generoFile.readLong();
                
                if (generoAtual.equals(genero)) {
                    posUltimo = posAtual;
                    while (proxPos != -1) {
                        posUltimo = proxPos;
                        generoFile.seek(proxPos);
                        generoFile.readUTF(); // Pula o gênero
                        generoFile.readInt(); // Pula o ID
                        proxPos = generoFile.readLong();
                    }
                    
                    // Atualiza o ponteiro do último nó
                    generoFile.seek(posUltimo + generoAtual.length() + 1 + 4 + 8 - 12);
                    generoFile.writeLong(novaPos);
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao inserir gênero '" + genero + "' no offset " + generoFile.getFilePointer() + ": " + e.getMessage());
        }
    }

    private void inserirPlataforma(String plataforma, int appid) throws IOException {
        try {
            if (plataformaFile == null) {
                System.err.println("Arquivo de plataforma não inicializado");
                return;
            }

            // Mesma lógica do inserirGenero
            long novaPos = plataformaFile.length();
            plataformaFile.seek(novaPos);
            plataformaFile.writeUTF(plataforma);
            plataformaFile.writeInt(appid);
            plataformaFile.writeLong(-1);

            plataformaFile.seek(0);
            long posUltimo = -1;
            
            while (plataformaFile.getFilePointer() < novaPos) {
                long posAtual = plataformaFile.getFilePointer();
                String plataformaAtual = plataformaFile.readUTF();
                plataformaFile.readInt();
                long proxPos = plataformaFile.readLong();
                
                if (plataformaAtual.equals(plataforma)) {
                    posUltimo = posAtual;
                    while (proxPos != -1) {
                        posUltimo = proxPos;
                        plataformaFile.seek(proxPos);
                        plataformaFile.readUTF();
                        plataformaFile.readInt();
                        proxPos = plataformaFile.readLong();
                    }
                    
                    plataformaFile.seek(posUltimo + plataformaAtual.length() + 1 + 4 + 8 - 12);
                    plataformaFile.writeLong(novaPos);
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao inserir plataforma '" + plataforma + "' no offset " + plataformaFile.getFilePointer() + ": " + e.getMessage());
        }
    }

    public List<Integer> buscarPorGenero(String genero) {
        List<Integer> ids = new ArrayList<>();
        if (generoFile == null) {
            System.err.println("Arquivo de gênero não inicializado");
            return ids;
        }

        try {
            generoFile.seek(0);
            
            while (generoFile.getFilePointer() < generoFile.length()) {
                long posAtual = generoFile.getFilePointer();
                String generoAtual = generoFile.readUTF();
                int idAtual = generoFile.readInt();
                long proxPos = generoFile.readLong();

                if (generoAtual.equalsIgnoreCase(genero)) {
                    ids.add(idAtual);
                    
                    // Segue a cadeia de nós
                    while (proxPos != -1) {
                        generoFile.seek(proxPos);
                        generoAtual = generoFile.readUTF();
                        idAtual = generoFile.readInt();
                        proxPos = generoFile.readLong();
                        ids.add(idAtual);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao buscar por gênero: " + e.getMessage());
        }
        return ids;
    }

    public List<Integer> buscarPorPlataforma(String plataforma) {
        List<Integer> ids = new ArrayList<>();
        if (plataformaFile == null) {
            System.err.println("Arquivo de plataforma não inicializado");
            return ids;
        }

        try {
            plataformaFile.seek(0);
            
            while (plataformaFile.getFilePointer() < plataformaFile.length()) {
                long posAtual = plataformaFile.getFilePointer();
                String plataformaAtual = plataformaFile.readUTF();
                int idAtual = plataformaFile.readInt();
                long proxPos = plataformaFile.readLong();

                if (plataformaAtual.equalsIgnoreCase(plataforma)) {
                    ids.add(idAtual);
                    
                    // Segue a cadeia de nós
                    while (proxPos != -1) {
                        plataformaFile.seek(proxPos);
                        plataformaAtual = plataformaFile.readUTF();
                        idAtual = plataformaFile.readInt();
                        proxPos = plataformaFile.readLong();
                        ids.add(idAtual);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao buscar por plataforma: " + e.getMessage());
        }
        return ids;
    }

    public List<Integer> buscarPorGeneroEPlataforma(String genero, String plataforma) {
        List<Integer> porGenero = buscarPorGenero(genero);
        List<Integer> porPlataforma = buscarPorPlataforma(plataforma);
        
        porGenero.retainAll(porPlataforma);
        return porGenero;
    }

    public void listarTudo() {
        try {
            System.out.println("=== Lista por Gênero ===");
            listarArquivo(generoFile);
            
            System.out.println("\n=== Lista por Plataforma ===");
            listarArquivo(plataformaFile);
        } catch (IOException e) {
            System.err.println("Erro ao listar conteúdo: " + e.getMessage());
        }
    }

    private void listarArquivo(RandomAccessFile file) throws IOException {
        if (file == null) {
            System.err.println("Arquivo não inicializado");
            return;
        }

        file.seek(0);
        while (file.getFilePointer() < file.length()) {
            String atributo = file.readUTF();
            int id = file.readInt();
            long prox = file.readLong();
            System.out.println(atributo + " - " + id + " - próximo: " + prox);
        }
    }

    public void close() {
        try {
            if (generoFile != null) {
                generoFile.close();
            }
            if (plataformaFile != null) {
                plataformaFile.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar arquivos: " + e.getMessage());
        }
    }
}
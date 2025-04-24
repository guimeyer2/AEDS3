package controller;

import java.io.*;
import java.util.*;
import Model.steam;

public class ExternalSort {
    // Caminho do arquivo principal e diretório temporário para arquivos intermediários
    private String filePath = "TP1/src/steam.db";
    private String tempDir = "TP1/src/temp/";

    // Método principal para ordenação externa
    public void externalSort(int numCaminhos, int maxRegistrosMemoria) {
        try {
            // Criando diretório temporário caso não exista
            File tempDirectory = new File(tempDir);
            if (!tempDirectory.exists()) {
                tempDirectory.mkdirs();
            }

            List<File> tempFiles = new ArrayList<>();
            RandomAccessFile file = new RandomAccessFile(filePath, "rw");
            long pos = 12; // Pula o cabeçalho do arquivo
            file.seek(pos);

            List<byte[]> buffer = new ArrayList<>();

            // Lendo registros do arquivo principal e armazenando no buffer
            while (file.getFilePointer() < file.length()) {
                byte tombstone = file.readByte(); // Lendo marcador de exclusão
                int tam = file.readInt(); // Tamanho do registro
                byte[] arr = new byte[tam];
                file.read(arr); // Lendo registro completo

                if (tombstone == 0) { // Se o registro não foi deletado, adiciona ao buffer
                    buffer.add(arr);
                }

                // Se o buffer atingir o limite, escreve um arquivo temporário ordenado
                if (buffer.size() >= maxRegistrosMemoria) {
                    tempFiles.add(writeTempFile(buffer));
                    buffer.clear();
                }
            }
            file.close();

            // Se houver registros restantes no buffer, grava em um último arquivo temporário
            if (!buffer.isEmpty()) {
                tempFiles.add(writeTempFile(buffer));
            }

            // Mescla os arquivos temporários ordenados em um único arquivo final
            mergeSortedFiles(tempFiles, numCaminhos);
            System.out.println("Arquivo ordenado e salvo em: " + filePath);
        } catch (IOException e) {
            System.err.println("Erro na ordenação externa: " + e);
        }
    }

    // Método para escrever um arquivo temporário ordenado
    private File writeTempFile(List<byte[]> registros) throws IOException {
        // Ordena os registros pelo ID do jogo
        registros.sort(Comparator.comparingInt(arr -> {
            steam temp = new steam();
            temp.fromByteArray(arr);
            return temp.getAppid();
        }));

        // Cria um novo arquivo temporário para armazenar os registros ordenados
        File tempFile = File.createTempFile("sorted", ".tmp", new File(tempDir));
        try (RandomAccessFile tempRAF = new RandomAccessFile(tempFile, "rw")) {
            for (byte[] reg : registros) {
                tempRAF.writeInt(reg.length); // Escreve o tamanho do registro
                tempRAF.write(reg); // Escreve o registro
            }
        }
        return tempFile;
    }

    // Método para mesclar os arquivos temporários ordenados em um único arquivo final
    private void mergeSortedFiles(List<File> tempFiles, int numCaminhos) throws IOException {
        // Fila de prioridade para manter os registros em ordem crescente
        PriorityQueue<TempFileReader> pq = new PriorityQueue<>(Comparator.comparingInt(t -> t.currentAppId));
        RandomAccessFile sortedFile = new RandomAccessFile(filePath, "rw");
        sortedFile.setLength(0);
        sortedFile.writeInt(0); // Inicializa maxId como 0
        sortedFile.writeLong(12); // Inicializa lastPos como 12 (início dos dados)
        long lastPos = 12;
        int maxId = 0;

        List<TempFileReader> readers = new ArrayList<>();

        // Inicializa leitores para cada arquivo temporário
        for (File file : tempFiles) {
            TempFileReader reader = new TempFileReader(file);
            if (reader.hasNext()) {
                pq.add(reader);
            }
            readers.add(reader);
        }

        // Processo de intercalação dos arquivos ordenados
        while (!pq.isEmpty()) {
            TempFileReader reader = pq.poll();
            if (reader.currentRecord == null) continue;

            byte[] reg = reader.currentRecord;
            steam temp = new steam();
            temp.fromByteArray(reg);

            // Escrevendo registro ordenado no arquivo final
            sortedFile.seek(lastPos);
            sortedFile.writeByte(0); // Marca como registro ativo
            sortedFile.writeInt(reg.length); // Escreve o tamanho do registro
            sortedFile.write(reg); // Escreve os dados
            lastPos = sortedFile.getFilePointer();

            maxId = Math.max(maxId, temp.getAppid()); // Atualiza o maior ID encontrado

            // Se houver mais registros no arquivo, adiciona de volta à fila de prioridade
            if (reader.hasNext()) {
                reader.readNext();
                pq.add(reader);
            }
        }

        // Atualiza cabeçalho do arquivo ordenado
        sortedFile.seek(0);
        sortedFile.writeInt(maxId); // Atualiza o maior ID
        sortedFile.writeLong(lastPos); // Atualiza a posição do final do arquivo
        sortedFile.close();

        // Fecha todos os leitores e remove arquivos temporários
        for (TempFileReader reader : readers) {
            reader.close();
        }
    }

    // Classe auxiliar para leitura dos arquivos temporários ordenados
    private static class TempFileReader {
        private RandomAccessFile raf;
        private byte[] currentRecord;
        private int currentAppId;

        public TempFileReader(File file) throws IOException {
            this.raf = new RandomAccessFile(file, "r");
            readNext();
        }

        // Lê o próximo registro do arquivo temporário
        public void readNext() throws IOException {
            if (raf.getFilePointer() < raf.length()) {
                int tam = raf.readInt(); // Lê o tamanho do registro
                if (tam <= 0 || raf.getFilePointer() + tam > raf.length()) {
                    currentRecord = null;
                    return;
                }
                currentRecord = new byte[tam];
                raf.read(currentRecord);
                steam temp = new steam();
                temp.fromByteArray(currentRecord);
                currentAppId = temp.getAppid(); // Obtém o ID do jogo
            } else {
                currentRecord = null;
            }
        }

        // Verifica se ainda há registros a serem lidos
        public boolean hasNext() {
            return currentRecord != null;
        }

        // Fecha o arquivo temporário
        public void close() throws IOException {
            raf.close();
        }
    }
}
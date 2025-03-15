package controller;

import java.io.*;
import java.util.*;
import Model.steam;

public class ExternalSort {
    private String filePath = "TP1/src/steam.db";
    private String tempDir = "TP1/src/temp/";

    public void externalSort(int numCaminhos, int maxRegistrosMemoria) {
        try {
            File tempDirectory = new File(tempDir);
            if (!tempDirectory.exists()) {
                tempDirectory.mkdirs();
            }

            List<File> tempFiles = new ArrayList<>();
            RandomAccessFile file = new RandomAccessFile(filePath, "rw");
            long pos = 12;
            file.seek(pos);

            List<byte[]> buffer = new ArrayList<>();
            while (file.getFilePointer() < file.length()) {
                byte tombstone = file.readByte();
                int tam = file.readInt();
                byte[] arr = new byte[tam];
                file.read(arr);

                if (tombstone == 0) {
                    buffer.add(arr);
                }

                if (buffer.size() >= maxRegistrosMemoria) {
                    tempFiles.add(writeTempFile(buffer));
                    buffer.clear();
                }
            }
            file.close();

            if (!buffer.isEmpty()) {
                tempFiles.add(writeTempFile(buffer));
            }

            mergeSortedFiles(tempFiles, numCaminhos);
            System.out.println("Arquivo ordenado e salvo em: " + filePath);
        } catch (IOException e) {
            System.err.println("Erro na ordenação externa: " + e);
        }
    }

    private File writeTempFile(List<byte[]> registros) throws IOException {
        registros.sort(Comparator.comparingInt(arr -> {
            steam temp = new steam();
            temp.fromByteArray(arr);
            return temp.getAppid();
        }));

        File tempFile = File.createTempFile("sorted", ".tmp", new File(tempDir));
        try (RandomAccessFile tempRAF = new RandomAccessFile(tempFile, "rw")) {
            for (byte[] reg : registros) {
                tempRAF.writeInt(reg.length);
                tempRAF.write(reg);
            }
        }
        return tempFile;
    }

    private void mergeSortedFiles(List<File> tempFiles, int numCaminhos) throws IOException {
        PriorityQueue<TempFileReader> pq = new PriorityQueue<>(Comparator.comparingInt(t -> t.currentAppId));
        RandomAccessFile sortedFile = new RandomAccessFile(filePath, "rw");
        sortedFile.setLength(0);
        sortedFile.writeInt(0);
        sortedFile.writeLong(12);
        long lastPos = 12;
        int maxId = 0;

        List<TempFileReader> readers = new ArrayList<>();
        for (File file : tempFiles) {
            TempFileReader reader = new TempFileReader(file);
            if (reader.hasNext()) {
                pq.add(reader);
            }
            readers.add(reader);
        }

        while (!pq.isEmpty()) {
            TempFileReader reader = pq.poll();
            if (reader.currentRecord == null) continue;

            byte[] reg = reader.currentRecord;
            steam temp = new steam();
            temp.fromByteArray(reg);
            
            sortedFile.seek(lastPos);
            sortedFile.writeByte(0);
            sortedFile.writeInt(reg.length);
            sortedFile.write(reg);
            lastPos = sortedFile.getFilePointer();

            maxId = Math.max(maxId, temp.getAppid());

            if (reader.hasNext()) {
                reader.readNext();
                pq.add(reader);
            }
        }

        sortedFile.seek(0);
        sortedFile.writeInt(maxId);
        sortedFile.writeLong(lastPos);
        sortedFile.close();

        for (TempFileReader reader : readers) {
            reader.close();
        }
    }

    private static class TempFileReader {
        private RandomAccessFile raf;
        private byte[] currentRecord;
        private int currentAppId;

        public TempFileReader(File file) throws IOException {
            this.raf = new RandomAccessFile(file, "r");
            readNext();
        }

        public void readNext() throws IOException {
            if (raf.getFilePointer() < raf.length()) {
                int tam = raf.readInt();
                if (tam <= 0 || raf.getFilePointer() + tam > raf.length()) {
                    currentRecord = null;
                    return;
                }
                currentRecord = new byte[tam];
                raf.read(currentRecord);
                steam temp = new steam();
                temp.fromByteArray(currentRecord);
                currentAppId = temp.getAppid();
            } else {
                currentRecord = null;
            }
        }

        public boolean hasNext() {
            return currentRecord != null;
        }

        public void close() throws IOException {
            raf.close();
        }
    }
}
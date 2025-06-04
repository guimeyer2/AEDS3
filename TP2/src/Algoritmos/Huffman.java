package Algoritmos;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;

// Tirei do actions pra não dar confusão com os outros algoritmos
public class Huffman {

    
    private static final String nomeAlgoritmo = "Huffman";

    // Nó da árvore de Huffman.
     
    static class HuffmanNode implements Comparable<HuffmanNode> {
        byte character; 
        int frequency;  
        HuffmanNode leftChild;
        HuffmanNode rightChild;

        // Construtor para nó folha
        public HuffmanNode(byte character, int frequency) {
            this.character = character;
            this.frequency = frequency;
        }

        // Construtor para nó interno
        public HuffmanNode(int frequency, HuffmanNode leftChild, HuffmanNode rightChild) {
            this.frequency = frequency;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
        }

        
        public boolean isLeaf() {
            return this.leftChild == null && this.rightChild == null;
        }

        
        @Override
        public int compareTo(HuffmanNode other) {
            return this.frequency - other.frequency;
        }
    }

    
    public static class HuffmanResult {
        public final long tamanhoOriginal;
        public final long tamanhoComprimido; 
        public final long tempoPercorridoMillis;
        public final String algorithmName = nomeAlgoritmo;
        public final double porcentagemCompressao; // Em porcentagem

        public HuffmanResult(long tamanhoOriginal
        , long tamanhoComprimido, long tempoPercorridoMillis) {
            this.tamanhoOriginal
             = tamanhoOriginal
            ;
            this.tamanhoComprimido = tamanhoComprimido;
            this.tempoPercorridoMillis = tempoPercorridoMillis;
            if (tamanhoOriginal
             > 0) {
                this.porcentagemCompressao = ((double) (tamanhoOriginal
                 - tamanhoComprimido) / tamanhoOriginal
                ) * 100.0;
            } else {
                this.porcentagemCompressao = 0.0;
            }
        }
        
        public HuffmanResult(long tamanhoPosCompressao, long tempoPercorridoMillis) {
            this.tamanhoOriginal
             = tamanhoPosCompressao; 
            this.tamanhoComprimido = -1; 
            this.tempoPercorridoMillis = tempoPercorridoMillis;
            this.porcentagemCompressao = -1;
        }


        @Override
        public String toString() {
            if (tamanhoComprimido != -1) { // Resultado da Compressão
                 return String.format(
                    "Algoritmo: %s\n" +
                    "Tempo de execução: %d ms\n" +
                    "Tamanho original: %d bytes\n" +
                    "Tamanho comprimido: %d bytes\n" +
                    "Taxa de compressão: %.2f%%",
                    algorithmName, tempoPercorridoMillis, tamanhoOriginal
                    , tamanhoComprimido, porcentagemCompressao
                );
            } else { // Resultado da Descompressão
                return String.format(
                    "Algoritmo: %s (Descompressão)\n" +
                    "Tempo de execução: %d ms\n" +
                    "Tamanho do arquivo gerado: %d bytes",
                    algorithmName, tempoPercorridoMillis, tamanhoOriginal

                );
            }
        }
    }

    
    public HuffmanResult compress(String inputFilePath, String outputFilePath) throws IOException {
        long startTime = System.currentTimeMillis();

        // le todos os bytes do arquivo de entrada
        byte[] fileBytes = Files.readAllBytes(Paths.get(inputFilePath));
        if (fileBytes.length == 0) {
            
            try (FileOutputStream fos = new FileOutputStream(outputFilePath);
                 DataOutputStream dos = new DataOutputStream(fos)) {
                dos.writeInt(0); 
                dos.writeLong(0); 
            }
            return new HuffmanResult(0, Files.size(Paths.get(outputFilePath)), System.currentTimeMillis() - startTime);
        }


        // constroi o mapa de frequência dos bytes
        Map<Byte, Integer> frequencyMap = buildFrequencyMap(fileBytes);

        // constroi a árvore de Huffman
        HuffmanNode rootNode = buildHuffmanTree(frequencyMap);

        // gera os códigos de Huffman para cada byte
        Map<Byte, String> huffmanCodes = new HashMap<>();
        generateHuffmanCodes(rootNode, "", huffmanCodes);

        // escreveo arquivo comprimido
        try (FileOutputStream fos = new FileOutputStream(outputFilePath);
             DataOutputStream dos = new DataOutputStream(fos)) {

            
            dos.writeInt(frequencyMap.size());
            for (Map.Entry<Byte, Integer> entry : frequencyMap.entrySet()) {
                dos.writeByte(entry.getKey());
                dos.writeInt(entry.getValue());
            }

            
            dos.writeLong(fileBytes.length);

            
            BitOutputStream bitOut = new BitOutputStream(dos);
            for (byte b : fileBytes) {
                String code = huffmanCodes.get(b);
                for (char bitChar : code.toCharArray()) {
                    bitOut.writeBit(bitChar - '0'); // '0' -> 0, '1' -> 1
                }
            }
            bitOut.close();

        }

        long tempoFinal = System.currentTimeMillis();
        long tamanhoOriginal
         = Files.size(Paths.get(inputFilePath));
        long compressedSize = Files.size(Paths.get(outputFilePath));

        return new HuffmanResult(tamanhoOriginal
        , compressedSize, tempoFinal - startTime);
    }

    
    public HuffmanResult decompress(String inputFilePath, String outputFilePath) throws IOException {
        long startTime = System.currentTimeMillis();

        try (FileInputStream fis = new FileInputStream(inputFilePath);
             DataInputStream dis = new DataInputStream(fis)) {

            // le a tabela de frequência
            int frequencyMapSize = dis.readInt();
            if (frequencyMapSize == 0) { 
                long originalLengthCheck = dis.readLong();
                if (originalLengthCheck == 0) {
                    // Cria um arquivo de saída vazio
                    Files.write(Paths.get(outputFilePath), new byte[0]);
                    long tempoFinal = System.currentTimeMillis();
                    return new HuffmanResult(0, tempoFinal - startTime);
                } else {
                    throw new IOException("Formato de arquivo comprimido inválido: arquivo vazio com tamanho original não zero.");
                }
            }

            Map<Byte, Integer> frequencyMap = new HashMap<>();
            for (int i = 0; i < frequencyMapSize; i++) {
                byte b = dis.readByte();
                int frequency = dis.readInt();
                frequencyMap.put(b, frequency);
            }

            // le o tamanho original do arquivo
            long originalLength = dis.readLong();

            // reconstrói a árvore de Huffman
            HuffmanNode rootNode = buildHuffmanTree(frequencyMap);

            // le os dados comprimidos e decodificar
            try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                BitInputStream bitIn = new BitInputStream(dis);
                HuffmanNode currentNode = rootNode;
                for (long i = 0; i < originalLength; i++) {
                  
                    if (rootNode.isLeaf()) { 
                        fos.write(rootNode.character);
                        continue; 
                    }
                    
                    currentNode = rootNode; 
                    while (!currentNode.isLeaf()) {
                        int bit = bitIn.readBit();
                        if (bit == -1) {
                            throw new IOException("Erro");
                        }
                        if (bit == 0) {
                            currentNode = currentNode.leftChild;
                        } else {
                            currentNode = currentNode.rightChild;
                        }
                        if (currentNode == null) {
                             throw new IOException("Caminho inválido na árvore de Huffman durante a descompressão.");
                        }
                    }
                    fos.write(currentNode.character);
                }
            }
        }

        long tempoFinal = System.currentTimeMillis();
        long decompressedSize = Files.size(Paths.get(outputFilePath));
        return new HuffmanResult(decompressedSize, tempoFinal - startTime);
    }

    
    private Map<Byte, Integer> buildFrequencyMap(byte[] data) {
        Map<Byte, Integer> map = new HashMap<>();
        for (byte b : data) {
            map.put(b, map.getOrDefault(b, 0) + 1);
        }
        return map;
    }

    
    private HuffmanNode buildHuffmanTree(Map<Byte, Integer> frequencyMap) {
        // Casos especiais
        if (frequencyMap.isEmpty()){
             
            return new HuffmanNode((byte)0,0); 
        }
        
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> entry : frequencyMap.entrySet()) {
            priorityQueue.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }
        
        // Caso especial: apenas um tipo de byte no arquivo.
        
        if (priorityQueue.size() == 1) {
            // Se há apenas um tipo de caractere, a árvore é apenas esse nó.
           
        }


        // Constrói a árvore combinando os dois nós de menor frequência
        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();
            HuffmanNode parent = new HuffmanNode(left.frequency + right.frequency, left, right);
            priorityQueue.add(parent);
        }
        return priorityQueue.poll(); 
    }

    
    private void generateHuffmanCodes(HuffmanNode node, String currentCode, Map<Byte, String> huffmanCodes) {
        if (node == null) {
            return;
        }
        if (node.isLeaf()) {
            // Caso especial: se a árvore tem apenas um nó (raiz é folha),
            
            if (currentCode.isEmpty() && huffmanCodes.isEmpty() && node.leftChild == null && node.rightChild == null) {
                 huffmanCodes.put(node.character, "0");
            } else {
                 huffmanCodes.put(node.character, currentCode);
            }
            return;
        }
        generateHuffmanCodes(node.leftChild, currentCode + "0", huffmanCodes);
        generateHuffmanCodes(node.rightChild, currentCode + "1", huffmanCodes);
    }


    // Classe auxiliar para escrever bits em um OutputStream.

    private static class BitOutputStream implements AutoCloseable {
        private DataOutputStream dos;
        private int currentByte;
        private int numBitsInCurrentByte;

        public BitOutputStream(DataOutputStream dos) {
            this.dos = dos;
            this.currentByte = 0;
            this.numBitsInCurrentByte = 0;
        }

        
        public void writeBit(int bit) throws IOException {
            if (bit != 0 && bit != 1) {
                throw new IllegalArgumentException("Bit deve ser 0 ou 1");
            }
            currentByte = (currentByte << 1) | bit;
            numBitsInCurrentByte++;
            if (numBitsInCurrentByte == 8) {
                dos.writeByte(currentByte);
                numBitsInCurrentByte = 0;
                currentByte = 0; 
            }
        }

        
        @Override
        public void close() throws IOException {
            if (numBitsInCurrentByte > 0) {
                currentByte <<= (8 - numBitsInCurrentByte); 
                dos.writeByte(currentByte);
            }
            
        }
    }

    // Classe auxiliar para ler bits de um InputStream.

    private static class BitInputStream {
        private DataInputStream dis;
        private int currentByte;
        private int numBitsRemainingInCurrentByte;

        public BitInputStream(DataInputStream dis) {
            this.dis = dis;
            this.currentByte = 0;
            this.numBitsRemainingInCurrentByte = 0;
        }

        
        public int readBit() throws IOException {
            if (numBitsRemainingInCurrentByte == 0) {
                int nextByte = dis.read(); 
                if (nextByte == -1) {
                    return -1;
                }
                currentByte = nextByte;
                numBitsRemainingInCurrentByte = 8;
            }
            // extrai o bit mais significativo (esquerdo)
            int bit = (currentByte >> (numBitsRemainingInCurrentByte - 1)) & 1;
            numBitsRemainingInCurrentByte--;
            return bit;
        }
         
    }


    // método para testes retirado
   
}

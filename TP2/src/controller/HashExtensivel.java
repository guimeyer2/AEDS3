package controller;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import Model.Registro;

public class HashExtensivel {
    private static final String ARQUIVO = "data/indexes/Hash.db";
    private RandomAccessFile file;
    private Diretorio diretorio;
    private int bucketSize;

    public HashExtensivel(int bucketSize) {
        this.bucketSize = bucketSize;
        try {
            file = new RandomAccessFile(ARQUIVO, "rw");
    
            if (file.length() <= 4) {
                inicializarArquivo();
            } else {
                try {
                    file.seek(0);
                    this.bucketSize = file.readInt();
                    carregarDiretorio();
                } catch (IOException e) {
                    System.err.println("Arquivo corrompido. Recriando...");
                    file.setLength(0); // Limpa o arquivo
                    inicializarArquivo();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void inicializarArquivo() throws IOException {
        file.seek(0);
        file.writeInt(bucketSize);  // 4 bytes
        
        // Escreve cabeçalho do diretório (8 bytes)
        file.writeInt(1); // profundidadeGlobal
        file.writeInt(2); // quantidade de buckets
        
        // Escreve os endereços dos buckets (2 * 8 bytes = 16 bytes)
        long posBucket1 = 28; // 4 (bucketSize) + 8 (header) + 16 (endereços) = 28
        long posBucket2 = posBucket1 + calculateBucketSize();
        
        file.writeLong(posBucket1);
        file.writeLong(posBucket2);
        
        // Agora escreve os buckets
        Bucket b1 = new Bucket(bucketSize);
        Bucket b2 = new Bucket(bucketSize);
        
        escreverBucketNaPosicao(b1, posBucket1);
        escreverBucketNaPosicao(b2, posBucket2);
    }
    private int calculateBucketSize() {
        // Tamanho fixo estimado para um bucket vazio
        return 12; // 4 (profundidadeLocal) + 4 (maxRegistros) + 4 (size=0)
    }
    private void escreverBucketNaPosicao(Bucket bucket, long pos) throws IOException {
        byte[] bytes = bucket.toByteArray();
        file.seek(pos);
        file.write(bytes);
    }

    private void salvarDiretorio() throws IOException {
        file.seek(4); // Pula o tamanho do bucket (4 bytes)

        file.writeInt(diretorio.profundidadeGlobal);
    
        int tamanho = (int) Math.pow(2, diretorio.profundidadeGlobal);
        for (int i = 0; i < tamanho; i++) {
            file.writeLong(diretorio.enderecosBuckets[i]);
        }
    }

    private void carregarDiretorio() throws IOException {
        file.seek(4); // Pula o bucketSize
        
        diretorio = new Diretorio(file.readInt());
        int numBuckets = file.readInt();
        
        for (int i = 0; i < numBuckets; i++) {
            diretorio.enderecosBuckets[i] = file.readLong();
        }
    }

    private long escreverBucket(Bucket bucket) throws IOException {
        byte[] bytes = bucket.toByteArray();
        long pos = file.length(); // Aponta para o final do arquivo
    
        file.seek(pos); // Vai para o fim do arquivo
        file.writeInt(bytes.length); // Grava o tamanho
        file.write(bytes);           // Grava os dados reais
    
        return pos; // Retorna o endereço do bucket
    }

    private void escreverBucketComEndereco(Bucket b, long endereco) throws IOException {
        byte[] bytes = b.toByteArray();
        
        file.seek(endereco);
        file.writeInt(bytes.length);
        file.write(bytes);
    }

    private Bucket lerBucket(long endereco) throws IOException {
    if (endereco < 28) { // Os primeiros 28 bytes são do cabeçalho
        throw new IOException("Endereço inválido para bucket: " + endereco);
    }
    
    file.seek(endereco);
    
    Bucket bucket = new Bucket(bucketSize);
    
    try {
        bucket.profundidadeLocal = file.readInt();
        bucket.maxRegistros = file.readInt();
        int size = file.readInt();
        
        // Validação mais flexível para o número de registros
        if (size < 0 || size > bucket.maxRegistros * 2) {
            throw new IOException("Número inválido de registros: " + size);
        }
        
        bucket.registros = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Registro r = new Registro();
            r.id = file.readInt();
            r.end = file.readLong();
            bucket.registros.add(r);
        }
    } catch (EOFException e) {
        throw new IOException("Bucket incompleto no endereço " + endereco, e);
    }
    
    return bucket;
}
    


    public synchronized void inserir(Registro r) {
        try {
            int hash = diretorio.hash(r.id);
            long endereco = diretorio.enderecosBuckets[hash];
            if (endereco < 0) {
                // Criar novo bucket se o endereço for inválido
                Bucket novoBucket = new Bucket(bucketSize);
                novoBucket.inserir(r);
                endereco = escreverBucket(novoBucket);
                diretorio.enderecosBuckets[hash] = endereco;
                salvarDiretorio();
                return;
            }
            Bucket b = lerBucket(endereco);


                if (!b.inserir(r)) {
              
                    b.profundidadeLocal++;

                    // Se a profundidade local agora é maior que a global, duplicar diretório
                    if (b.profundidadeLocal > diretorio.profundidadeGlobal) {
                        diretorio.duplicar();
                        salvarDiretorio();
                    }

                    // Criar novo bucket
                    Bucket novoBucket = new Bucket(bucketSize);
                    novoBucket.profundidadeLocal = b.profundidadeLocal;

                    // Redistribuir registros
                    List<Registro> todos = new ArrayList<>(b.registros);
                    todos.add(r);  // adicionar o registro novo que causou o split
                    b.registros.clear();

                    for (Registro reg : todos) {
                        int novoHash = diretorio.hashComBits(reg.id, b.profundidadeLocal);
                        if ((novoHash & 1) == 0) {
                            b.registros.add(reg);
                        } else {
                            novoBucket.registros.add(reg);
                        }
                    }

                    long enderecoAntigo = endereco; // salve antes de reescrever
                    long enderecoNovo = escreverBucket(novoBucket);
                    enderecoAntigo = escreverBucket(b);

                    // Atualizar ponteiros do diretório
                    for (int i = 0; i < diretorio.enderecosBuckets.length; i++) {
                        int prefix = i >> (diretorio.profundidadeGlobal - b.profundidadeLocal);
                        if ((prefix & 1) == 0 && diretorio.enderecosBuckets[i] == endereco) {
                            diretorio.enderecosBuckets[i] = enderecoAntigo;
                        } else if ((prefix & 1) == 1 && diretorio.enderecosBuckets[i] == endereco) {
                            diretorio.enderecosBuckets[i] = enderecoNovo;
                        }
                    }

                    salvarDiretorio();
    
            } else {
                escreverBucketComEndereco(b, endereco);

            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Bucket novoBucket = new Bucket(bucketSize);
                novoBucket.inserir(r);
                long endereco = escreverBucket(novoBucket);
                // Atualizar diretório se necessário
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Registro buscar(int id) {
        try {
            int hash = diretorio.hash(id);
            long endereco = diretorio.enderecosBuckets[hash];
            Bucket b = lerBucket(endereco);
            return b.buscar(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
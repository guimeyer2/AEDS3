package Algoritmos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Diretorio {
    public int profundidadeGlobal;
    public long[] enderecosBuckets;

    public Diretorio(int profundidadeGlobal) {
        this.profundidadeGlobal = profundidadeGlobal;
        int tamanho = 1 << profundidadeGlobal;
        this.enderecosBuckets = new long[tamanho];
        Arrays.fill(enderecosBuckets, -1); // -1 significa sem bucket atribuído ainda
    }

    public int hash(int chave) {
        return chave & ((1 << profundidadeGlobal) - 1);
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(profundidadeGlobal);
        dos.writeInt(enderecosBuckets.length);

        for (long end : enderecosBuckets) {
            dos.writeLong(end);
        }

        return baos.toByteArray();
    }

    public void fromByteArray(byte[] array) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        DataInputStream dis = new DataInputStream(bais);

        profundidadeGlobal = dis.readInt();
        int tamanho = dis.readInt();
        enderecosBuckets = new long[tamanho];

        for (int i = 0; i < tamanho; i++) {
            enderecosBuckets[i] = dis.readLong();
        }
    }

    public void duplicar() {
        int novoTamanho = enderecosBuckets.length * 2;
        long[] novoEnderecos = new long[novoTamanho];
    
        for (int i = 0; i < enderecosBuckets.length; i++) {
            novoEnderecos[i] = enderecosBuckets[i];
            novoEnderecos[i + enderecosBuckets.length] = enderecosBuckets[i];
        }
    
        enderecosBuckets = novoEnderecos;
        profundidadeGlobal++;
    }
    
    public int hashComBits(int chave, int bits) {
        return chave & ((1 << bits) - 1);
    }
    
}

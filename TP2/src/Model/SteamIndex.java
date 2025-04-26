
//não utilizado 

/*package Model;
import Algoritmos.RegistroArvoreBMais;

import java.io.*;

public class SteamIndex implements RegistroArvoreBMais<SteamIndex> {
    private int appid;     // ID do jogo (chave primária)
    private long posicao;  // Posição no arquivo de dados

    public SteamIndex() {
        this(-1, -1);
    }

    public SteamIndex(int appid, long posicao) {
        this.appid = appid;
        this.posicao = posicao;
    }

    // Getters e Setters
    public int getAppid() { return appid; }
    public long getPosicao() { return posicao; }

    @Override
    public int compareTo(SteamIndex outro) {
        return Integer.compare(this.appid, outro.appid);
    }

    @Override
    public SteamIndex clone() {
        return new SteamIndex(this.appid, this.posicao);
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(appid);
        dos.writeLong(posicao);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        this.appid = dis.readInt();
        this.posicao = dis.readLong();
    }

    @Override
    public int size() {
        return 12; // 4 (int) + 8 (long)
    }

    @Override
    public String toString() {
        return "ID: " + appid + " | Posição: " + posicao;
    }
} */
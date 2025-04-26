package Algoritmos;

import java.io.IOException;

public interface RegistroArvoreBMais<T> {
    public int compareTo(T outro);
    public T clone();
    public byte[] toByteArray() throws IOException;
    public void fromByteArray(byte[] ba) throws IOException;
    public int size();
}
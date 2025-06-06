package Algoritmos;

import java.io.*;
import java.util.*;

public class LZW {

    // Compressão
    public static byte[] compress(byte[] input) {
        Map<String, Integer> dictionary = new HashMap<>();
        int dictSize = 256;

        // Inicializa o dicionário com todos os bytes possíveis
        for (int i = 0; i < 256; i++) {
            dictionary.put("" + (char) i, i);
        }

        String w = "";
        List<Integer> result = new ArrayList<>();

        for (byte b : input) {
            char c = (char) (b & 0xFF);
            String wc = w + c;
            if (dictionary.containsKey(wc)) {
                w = wc;
            } else {
                result.add(dictionary.get(w));
                dictionary.put(wc, dictSize++);
                w = "" + c;
            }
        }

        if (!w.equals("")) {
            result.add(dictionary.get(w));
        }

        // Converter lista de Integers para array de bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            for (int code : result) {
                dos.writeInt(code);
            }
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    // Descompressão
    public static byte[] decompress(byte[] compressed) {
        Map<Integer, String> dictionary = new HashMap<>();
        int dictSize = 256;

        // Inicializa o dicionário com todos os bytes possíveis
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, "" + (char) i);
        }

        List<Integer> codes = new ArrayList<>();
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(compressed));

        try {
            while (dis.available() > 0) {
                codes.add(dis.readInt());
            }
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String w = "" + (char) (codes.get(0).intValue());
        StringBuilder result = new StringBuilder(w);

        for (int i = 1; i < codes.size(); i++) {
            int k = codes.get(i);
            String entry;
            if (dictionary.containsKey(k)) {
                entry = dictionary.get(k);
            } else if (k == dictSize) {
                entry = w + w.charAt(0);
            } else {
                throw new IllegalArgumentException("Código inválido: " + k);
            }

            result.append(entry);
            dictionary.put(dictSize++, w + entry.charAt(0));
            w = entry;
        }

        return result.toString().getBytes();
    }
}


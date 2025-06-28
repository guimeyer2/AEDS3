package Algoritmos;

public class CriptografiaSimples {

    // --- SUBSTITUIÇÃO (César com deslocamento fixo) ---
    public static String substituir(String texto, int chave) {
        StringBuilder resultado = new StringBuilder();
        for (char c : texto.toCharArray()) {
            resultado.append((char) (c + chave));
        }
        return resultado.toString();
    }

    public static String reverterSubstituicao(String texto, int chave) {
        return substituir(texto, -chave);
    }

    // --- VIGENÈRE ---
    public static String vigenere(String texto, String chave) {
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            char k = chave.charAt(i % chave.length());
            resultado.append((char) (c + k % 256));
        }
        return resultado.toString();
    }

    public static String reverterVigenere(String texto, String chave) {
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            char k = chave.charAt(i % chave.length());
            resultado.append((char) (c - k % 256));
        }
        return resultado.toString();
    }

    // --- TRANSPOSIÇÃO POR COLUNAS ---
    public static String transposicaoColunas(String texto, int colunas) {
        int linhas = (int) Math.ceil((double) texto.length() / colunas);
        char[][] matriz = new char[linhas][colunas];

        int idx = 0;
        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {
                if (idx < texto.length()) {
                    matriz[l][c] = texto.charAt(idx++);
                } else {
                    matriz[l][c] = ' ';
                }
            }
        }

        StringBuilder resultado = new StringBuilder();
        for (int c = 0; c < colunas; c++) {
            for (int l = 0; l < linhas; l++) {
                resultado.append(matriz[l][c]);
            }
        }
        return resultado.toString();
    }

    public static String reverterTransposicaoColunas(String texto, int colunas) {
        int linhas = (int) Math.ceil((double) texto.length() / colunas);
        char[][] matriz = new char[linhas][colunas];

        int idx = 0;
        for (int c = 0; c < colunas; c++) {
            for (int l = 0; l < linhas; l++) {
                if (idx < texto.length()) {
                    matriz[l][c] = texto.charAt(idx++);
                }
            }
        }

        StringBuilder resultado = new StringBuilder();
        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {
                resultado.append(matriz[l][c]);
            }
        }
        return resultado.toString().trim();
    }
}

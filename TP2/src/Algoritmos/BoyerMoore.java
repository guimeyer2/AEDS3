package Algoritmos;

public class BoyerMoore {

    private final int ALPHABET_SIZE = 256;

    private int[] processamentoPorCaractereRuim(String pattern) {
        int m = pattern.length();
        int[] tabelaCaracRuim = new int[ALPHABET_SIZE];
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            tabelaCaracRuim[i] = -1;
        }
        for (int i = 0; i < m; i++) {
            
            char c = pattern.charAt(i);
            if (c < ALPHABET_SIZE) {
                tabelaCaracRuim[c] = i;
            }
        }
        return tabelaCaracRuim;
    }

    
    public int search(String texto, String pattern) {
        if (pattern == null || texto == null || pattern.isEmpty()) {
            return 0;
        }

        int m = pattern.length();
        int n = texto.length();
        if (m > n) {
            return 0;
        }

        int[] tabelaCaracRuim = processamentoPorCaractereRuim(pattern);
        int count = 0;
        int s = 0; 

        while (s <= (n - m)) {
            int j = m - 1;

            // Continua comparando da direita para a esquerda enquanto os caracteres correspondem
            while (j >= 0 && pattern.charAt(j) == texto.charAt(s + j)) {
                j--;
            }

            // Se o padrão foi encontrado (j < 0), incrementa a contagem e desloca o padrão
            if (j < 0) {
                count++;
                int shift = 1;
                if (s + m < n) {
                    char nextChar = texto.charAt(s + m);
                    
                    if (nextChar < ALPHABET_SIZE) {
                        shift = m - tabelaCaracRuim[nextChar];
                    } else {
                        shift = m + 1; // Salto padrão para caracteres desconhecidos/inválidos
                    }
                }
                s += shift;
            } else {
                // Desloca o padrão para alinhar o "mau caractere" do textoo com sua última ocorrência no padrão
                char caractereRuim = texto.charAt(s + j);
                int shift = 1;
            
                if (caractereRuim < ALPHABET_SIZE) {
                    shift = j - tabelaCaracRuim[caractereRuim];
                } else {
                    shift = j + 1; 
                }
                s += Math.max(1, shift);
            }
        }
        return count;
    }
}
package Algoritmos;

public class BoyerMoore {

    private final int ALPHABET_SIZE = 256;

    private int[] preprocessBadCharacterHeuristic(String pattern) {
        int m = pattern.length();
        int[] badCharTable = new int[ALPHABET_SIZE];
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            badCharTable[i] = -1;
        }
        for (int i = 0; i < m; i++) {
            
            char c = pattern.charAt(i);
            if (c < ALPHABET_SIZE) {
                badCharTable[c] = i;
            }
        }
        return badCharTable;
    }

    
    public int search(String text, String pattern) {
        if (pattern == null || text == null || pattern.isEmpty()) {
            return 0;
        }

        int m = pattern.length();
        int n = text.length();
        if (m > n) {
            return 0;
        }

        int[] badCharTable = preprocessBadCharacterHeuristic(pattern);
        int count = 0;
        int s = 0; 

        while (s <= (n - m)) {
            int j = m - 1;

            // Continua comparando da direita para a esquerda enquanto os caracteres correspondem
            while (j >= 0 && pattern.charAt(j) == text.charAt(s + j)) {
                j--;
            }

            // Se o padrão foi encontrado (j < 0), incrementa a contagem e desloca o padrão
            if (j < 0) {
                count++;
                int shift = 1;
                if (s + m < n) {
                    char nextChar = text.charAt(s + m);
                    
                    if (nextChar < ALPHABET_SIZE) {
                        shift = m - badCharTable[nextChar];
                    } else {
                        shift = m + 1; // Salto padrão para caracteres desconhecidos/inválidos
                    }
                }
                s += shift;
            } else {
                // Desloca o padrão para alinhar o "mau caractere" do texto com sua última ocorrência no padrão
                char badChar = text.charAt(s + j);
                int shift = 1;
            
                if (badChar < ALPHABET_SIZE) {
                    shift = j - badCharTable[badChar];
                } else {
                    shift = j + 1; 
                }
                s += Math.max(1, shift);
            }
        }
        return count;
    }
}
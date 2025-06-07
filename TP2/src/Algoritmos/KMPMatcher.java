package Algoritmos;

public class KMPMatcher {

    // Gera o vetor de prefixo 
    private int[] computePrefixFunction(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0;
        int i = 1;

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }

        return lps;
    }

    // Aplica o KMP para procurar todas as ocorrências
    public int search(String text, String pattern) {
        int n = text.length();
        int m = pattern.length();
        int[] lps = computePrefixFunction(pattern);

        int i = 0; // índice para text
        int j = 0; // índice para pattern
        int count = 0;

        while (i < n) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
            }

            if (j == m) {
                count++;
                j = lps[j - 1];
            } else if (i < n && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0)
                    j = lps[j - 1];
                else
                    i++;
            }
        }

        return count; // retorna quantas vezes o padrão aparece
    }
}

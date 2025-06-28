package Algoritmos;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CriptografiaModerna {

    private static final String ALGORITHM = "DES";
    private static final String CHAVE_SECRETA = "12345678"; // 8 bytes

    private static SecretKey gerarChave() {
        return new SecretKeySpec(CHAVE_SECRETA.getBytes(), ALGORITHM);
    }

    public static String criptografarDES(String textoClaro) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, gerarChave());
            byte[] encrypted = cipher.doFinal(textoClaro.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.err.println("Erro ao criptografar com DES: " + e.getMessage());
            return null;
        }
    }

    public static String descriptografarDES(String textoCriptografado) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, gerarChave());
            byte[] decoded = Base64.getDecoder().decode(textoCriptografado);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            System.err.println("Erro ao descriptografar com DES: " + e.getMessage());
            return null;
        }
    }
}

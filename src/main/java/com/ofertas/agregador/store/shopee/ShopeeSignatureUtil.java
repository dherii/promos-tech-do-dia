package com.ofertas.agregador.store.shopee;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * Implementa a assinatura exigida pela Shopee Affiliate Open API:
 * {@code Signature = SHA256(AppId + Timestamp + Payload + Secret)}, enviada no header
 * {@code Authorization: SHA256 Credential={appId}, Timestamp={ts}, Signature={sig}}.
 *
 * O timestamp deve ser Unix time em SEGUNDOS (não milissegundos) e próximo do
 * horário do servidor — diferenças de poucos minutos já causam erro de autenticação.
 */
final class ShopeeSignatureUtil {

    private ShopeeSignatureUtil() {
    }

    static String buildAuthorizationHeader(String appId, String secret, String payload) {
        long timestamp = Instant.now().getEpochSecond();
        String signature = sha256Hex(appId + timestamp + payload + secret);
        return "SHA256 Credential=%s, Timestamp=%d, Signature=%s".formatted(appId, timestamp, signature);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 é garantido pela JVM (padrão do java.security), então isso nunca deveria ocorrer
            throw new IllegalStateException("Algoritmo SHA-256 indisponível na JVM", ex);
        }
    }
}

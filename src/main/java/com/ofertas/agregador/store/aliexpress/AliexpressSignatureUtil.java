package com.ofertas.agregador.store.aliexpress;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;

public class AliexpressSignatureUtil {

    public static String signRequest(Map<String, String> params, String secret) {
        try {
            // 1. Pega todas as chaves e ordena alfabeticamente
            String[] keys = params.keySet().toArray(new String[0]);
            Arrays.sort(keys);

            // 2. Concatena Secret + chaves e valores + Secret
            StringBuilder query = new StringBuilder(secret);
            for (String key : keys) {
                String value = params.get(key);
                if (key != null && value != null) {
                    query.append(key).append(value);
                }
            }
            query.append(secret);

            // 3. Gera o hash MD5 em bytes
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(query.toString().getBytes(StandardCharsets.UTF_8));

            // 4. Converte para hexadecimal maiúsculo
            StringBuilder sign = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    sign.append("0");
                }
                sign.append(hex.toUpperCase());
            }
            return sign.toString();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar assinatura do AliExpress", e);
        }
    }
}
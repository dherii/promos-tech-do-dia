package com.ofertas.agregador.store.mercadolivre;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fluxo OAuth manual, de uso único por admin — NÃO é o webhook de notificações
 * (isso continua fora de escopo, por bom motivo: não retorna cupom sitewide).
 * Serve só pra obter o primeiro access_token + refresh_token; depois disso o
 * MercadoLivreTokenService mantém a renovação sozinho, sem precisar do tunnel.
 */
@RestController
public class MercadoLivreOAuthController {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivreOAuthController.class);

    private final MercadoLivreOAuthProperties properties;
    private final MercadoLivreTokenService tokenService;
    private final WebClient webClient = WebClient.builder().build();

    // Estado do PKCE entre /authorize e /callback — aceitável para um fluxo
    // manual de admin único; não é multi-usuário nem thread-safe pra isso.
    private final AtomicReference<String> pendingCodeVerifier = new AtomicReference<>();

    public MercadoLivreOAuthController(MercadoLivreOAuthProperties properties, MercadoLivreTokenService tokenService) {
        this.properties = properties;
        this.tokenService = tokenService;
    }

    @GetMapping("/api/mercadolivre/oauth/authorize")
    public ResponseEntity<Void> authorize() {
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        pendingCodeVerifier.set(codeVerifier);

        String url = properties.getAuthorizeUrl()
                + "?response_type=code"
                + "&client_id=" + properties.getClientId()
                + "&redirect_uri=" + properties.getRedirectUri()
                + "&code_challenge=" + codeChallenge
                + "&code_challenge_method=S256";

        return ResponseEntity.status(302).location(URI.create(url)).build();
    }

    @GetMapping("/api/mercadolivre/oauth/callback")
    public ResponseEntity<String> callback(@RequestParam String code) {
        String codeVerifier = pendingCodeVerifier.getAndSet(null);
        if (codeVerifier == null) {
            return ResponseEntity.badRequest()
                    .body("Sessão de autorização expirada — acesse /api/mercadolivre/oauth/authorize de novo.");
        }

        try {
            Map<String, Object> response = webClient.post()
                    .uri(properties.getTokenUrl())
                    .header("accept", "application/json")
                    .bodyValue(Map.of(
                            "grant_type", "authorization_code",
                            "client_id", properties.getClientId(),
                            "client_secret", properties.getClientSecret(),
                            "code", code,
                            "redirect_uri", properties.getRedirectUri(),
                            "code_verifier", codeVerifier
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("access_token")) {
                log.error("Falha ao trocar code por token: {}", response);
                return ResponseEntity.internalServerError().body("Falha ao obter token. Veja os logs.");
            }

            String accessToken = (String) response.get("access_token");
            String refreshToken = (String) response.get("refresh_token");
            int expiresIn = ((Number) response.get("expires_in")).intValue();

            tokenService.saveInitialTokens(accessToken, refreshToken, expiresIn);
            log.info("Tokens do Mercado Livre salvos com sucesso. Válido por {}s", expiresIn);

            return ResponseEntity.ok("Autorizado com sucesso! Pode fechar essa aba e desligar o cloudflared quando quiser.");

        } catch (Exception ex) {
            log.error("Erro ao trocar code por token", ex);
            return ResponseEntity.internalServerError().body("Erro: " + ex.getMessage());
        }
    }

    private String generateCodeVerifier() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateCodeChallenge(String verifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
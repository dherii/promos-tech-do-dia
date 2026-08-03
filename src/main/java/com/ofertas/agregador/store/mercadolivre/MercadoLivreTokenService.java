package com.ofertas.agregador.store.mercadolivre;

import com.ofertas.agregador.domain.PlatformConfig;
import com.ofertas.agregador.domain.Store;
import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.domain.repository.PlatformConfigRepository;
import com.ofertas.agregador.domain.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Guarda e renova o access_token do Mercado Livre em platform_config
 * (chaves ML_ACCESS_TOKEN, ML_REFRESH_TOKEN, ML_TOKEN_EXPIRES_AT).
 *
 * O Mercado Livre expira o access_token em poucas horas e ROTACIONA o
 * refresh_token a cada uso — por isso persistimos o refresh_token NOVO
 * a cada renovação, não só o access_token.
 */
@Service
public class MercadoLivreTokenService {

    private static final Logger log = LoggerFactory.getLogger(MercadoLivreTokenService.class);
    private static final String KEY_ACCESS_TOKEN = "ML_ACCESS_TOKEN";
    private static final String KEY_REFRESH_TOKEN = "ML_REFRESH_TOKEN";
    private static final String KEY_EXPIRES_AT = "ML_TOKEN_EXPIRES_AT";

    private final PlatformConfigRepository platformConfigRepository;
    private final StoreRepository storeRepository;
    private final MercadoLivreOAuthProperties oauthProperties;
    private final WebClient webClient = WebClient.builder().build();

    public MercadoLivreTokenService(PlatformConfigRepository platformConfigRepository,
                                     StoreRepository storeRepository,
                                     MercadoLivreOAuthProperties oauthProperties) {
        this.platformConfigRepository = platformConfigRepository;
        this.storeRepository = storeRepository;
        this.oauthProperties = oauthProperties;
    }

    /**
     * @return access_token válido, renovando via refresh_token se necessário,
     *         ou {@code null} se nunca foi autorizado.
     */
    public synchronized String getValidAccessToken() {
        Long storeId = resolveStoreId();
        if (storeId == null) {
            return null;
        }

        Optional<String> accessToken = readConfig(storeId, KEY_ACCESS_TOKEN);
        if (accessToken.isEmpty()) {
            return null; // nunca autorizado
        }

        boolean expired = readConfig(storeId, KEY_EXPIRES_AT)
                .map(raw -> LocalDateTime.parse(raw).isBefore(LocalDateTime.now().plusMinutes(2)))
                .orElse(true);

        return expired ? refreshToken(storeId) : accessToken.get();
    }

    public void saveInitialTokens(String accessToken, String refreshToken, int expiresInSeconds) {
        persistTokens(resolveStoreId(), accessToken, refreshToken, expiresInSeconds);
    }

    private String refreshToken(Long storeId) {
        Optional<String> refreshToken = readConfig(storeId, KEY_REFRESH_TOKEN);
        if (refreshToken.isEmpty()) {
            log.warn("Token do Mercado Livre expirado e sem refresh_token salvo — " +
                    "acesse /api/mercadolivre/oauth/authorize novamente");
            return null;
        }

        try {
            Map<String, Object> response = webClient.post()
                    .uri(oauthProperties.getTokenUrl())
                    .header("accept", "application/json")
                    .bodyValue(Map.of(
                            "grant_type", "refresh_token",
                            "client_id", oauthProperties.getClientId(),
                            "client_secret", oauthProperties.getClientSecret(),
                            "refresh_token", refreshToken.get()
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("access_token")) {
                log.error("Resposta inesperada ao renovar token do Mercado Livre: {}", response);
                return null;
            }

            String newAccessToken = (String) response.get("access_token");
            String newRefreshToken = (String) response.get("refresh_token");
            int expiresIn = ((Number) response.get("expires_in")).intValue();

            persistTokens(storeId, newAccessToken, newRefreshToken, expiresIn);
            log.info("Token do Mercado Livre renovado com sucesso, válido por {}s", expiresIn);
            return newAccessToken;

        } catch (Exception ex) {
            log.error("Falha ao renovar token do Mercado Livre — pode ser necessário reautorizar", ex);
            return null;
        }
    }

    private void persistTokens(Long storeId, String accessToken, String refreshToken, int expiresInSeconds) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresInSeconds);
        upsertConfig(storeId, KEY_ACCESS_TOKEN, accessToken);
        upsertConfig(storeId, KEY_REFRESH_TOKEN, refreshToken);
        upsertConfig(storeId, KEY_EXPIRES_AT, expiresAt.toString());
    }

    private Optional<String> readConfig(Long storeId, String key) {
        return platformConfigRepository.findByStoreIdAndConfigKey(storeId, key)
                .map(PlatformConfig::getConfigValue);
    }

    private void upsertConfig(Long storeId, String key, String value) {
        PlatformConfig config = platformConfigRepository.findByStoreIdAndConfigKey(storeId, key)
                .orElseGet(() -> new PlatformConfig(storeRepository.getReferenceById(storeId), key, value));
        config.setConfigValue(value);
        platformConfigRepository.save(config);
    }

    private Long resolveStoreId() {
        return storeRepository.findByCode(StoreType.MERCADO_LIVRE)
                .map(Store::getId)
                .orElse(null);
    }
}
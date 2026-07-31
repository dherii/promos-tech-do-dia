package com.ofertas.agregador.store.mercadolivre;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Vincula {@code mercadolivre.*} do application.properties.
 *
 * A lista de itens rastreados e seus links de afiliado NÃO ficam mais aqui —
 * foram para a tabela `mercadolivre_tracked_item` (ver MercadoLivreTrackedItem),
 * justamente para permitir adicionar produto sem reiniciar a aplicação.
 *
 * Exemplo application.properties:
 * <pre>
 * mercadolivre.api-base-url=https://api.mercadolibre.com
 * mercadolivre.access-token=${ML_ACCESS_TOKEN:}
 * </pre>
 */
@ConfigurationProperties(prefix = "mercadolivre")
public class MercadoLivreProperties {

    private String apiBaseUrl = "https://api.mercadolibre.com";
    private String accessToken;

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
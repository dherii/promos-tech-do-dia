package com.ofertas.agregador.store.mercadolivre;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Vincula as propriedades {@code mercadolivre.*} do application.properties.
 *
 * Exemplo application.properties:
 * <pre>
 * mercadolivre.api-base-url=https://api.mercadolibre.com
 * mercadolivre.access-token=${ML_ACCESS_TOKEN:}
 * mercadolivre.tracked-item-ids=MLB123456,MLB789012
 * mercadolivre.affiliate-query-params=matt_word=SEUCODIGO&matt_tool=88888888
 * </pre>
 *
 * IMPORTANTE: {@code affiliate-query-params} deve ser copiado EXATAMENTE do link
 * que o Portal do Afiliado (mercadolivre.com.br/l/afiliados-home) gera pra você —
 * o formato (matt_word+matt_tool vs affiliate=ID) varia por conta.
 */
@ConfigurationProperties(prefix = "mercadolivre")
public class MercadoLivreProperties {

    private String apiBaseUrl = "https://api.mercadolibre.com";
    private String accessToken;
    private List<String> trackedItemIds = List.of();
    private String affiliateQueryParams;

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

    public List<String> getTrackedItemIds() {
        return trackedItemIds;
    }

    public void setTrackedItemIds(List<String> trackedItemIds) {
        this.trackedItemIds = trackedItemIds;
    }

    public String getAffiliateQueryParams() {
        return affiliateQueryParams;
    }

    public void setAffiliateQueryParams(String affiliateQueryParams) {
        this.affiliateQueryParams = affiliateQueryParams;
    }
}

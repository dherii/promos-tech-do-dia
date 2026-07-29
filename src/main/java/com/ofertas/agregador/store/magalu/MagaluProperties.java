package com.ofertas.agregador.store.magalu;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Vincula {@code magalu.*} do application.properties.
 *
 * Assim como o Mercado Livre, o Magalu (Parceiro Magalu) não tem API pública
 * para geração de link — o link sai do Portal Parceiro Magalu manualmente.
 * affiliateQueryParams deve ser copiado de um link real gerado lá.
 */
@ConfigurationProperties(prefix = "magalu")
public class MagaluProperties {

    private String affiliateQueryParams;

    public String getAffiliateQueryParams() {
        return affiliateQueryParams;
    }

    public void setAffiliateQueryParams(String affiliateQueryParams) {
        this.affiliateQueryParams = affiliateQueryParams;
    }
}

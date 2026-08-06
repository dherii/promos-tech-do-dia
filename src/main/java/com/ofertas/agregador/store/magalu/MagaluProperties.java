package com.ofertas.agregador.store.magalu;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Vincula {@code magalu.*} do application.properties.
 *
 * Descoberta ao inspecionar seus links reais do Portal Parceiro Magalu: o
 * link de afiliado usa partner_id + promoter_id como query params FIXOS da
 * conta (diferente do Mercado Livre, aqui não muda por produto) — por isso
 * dá pra reconstruir por fórmula, sem precisar de link manual por item.
 *
 * Exemplo application.properties:
 * <pre>
 * magalu.partner-id=3440
 * magalu.promoter-id=5773802
 * </pre>
 */
@ConfigurationProperties(prefix = "magalu")
public class MagaluProperties {

    private String partnerId;
    private String promoterId;

    public String getPartnerId() { return partnerId; }
    public void setPartnerId(String partnerId) { this.partnerId = partnerId; }
    public String getPromoterId() { return promoterId; }
    public void setPromoterId(String promoterId) { this.promoterId = promoterId; }
}
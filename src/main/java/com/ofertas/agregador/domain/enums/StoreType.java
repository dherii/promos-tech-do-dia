package com.ofertas.agregador.domain.enums;

/**
 * Lojas/plataformas suportadas pelo agregador.
 * Cada valor deve ter uma implementação correspondente de
 * {@code AffiliateLinkGenerator} e {@code ProductFetcher}.
 */
public enum StoreType {
    SHOPEE,
    AMAZON,
    MERCADO_LIVRE,
    MAGALU,
    KABUM,
    ALIEXPRESS
}

package com.ofertas.agregador.store.mercadolivre;

import java.time.LocalDateTime;

/**
 * Projeção de saída — evita expor a entidade JPA diretamente na API.
 */
public record TrackedItemView(String itemId, String affiliateUrl, boolean active, LocalDateTime createdAt) {

    static TrackedItemView from(MercadoLivreTrackedItem entity) {
        return new TrackedItemView(entity.getItemId(), entity.getAffiliateUrl(), entity.isActive(), entity.getCreatedAt());
    }
}
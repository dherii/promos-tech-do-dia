package com.ofertas.agregador.store.mercadolivre;

/**
 * Corpo esperado no POST /api/mercadolivre/tracked-items.
 */
public record TrackedItemRequest(String itemId, String affiliateUrl) {

    public boolean isValid() {
        return itemId != null && !itemId.isBlank()
                && affiliateUrl != null && !affiliateUrl.isBlank();
    }
}
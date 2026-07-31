package com.ofertas.agregador.store.mercadolivre;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Fica no pacote store.mercadolivre (não em domain) de propósito — é uma
 * configuração específica dessa loja, não um conceito de domínio geral
 * (diferente de Product/Store, que são genéricos entre lojas).
 */
@Entity
@Table(name = "mercadolivre_tracked_item")
public class MercadoLivreTrackedItem {

    @Id
    @Column(name = "item_id", length = 50)
    private String itemId;

    @Column(name = "affiliate_url", nullable = false, columnDefinition = "TEXT")
    private String affiliateUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected MercadoLivreTrackedItem() {
        // exigido pelo JPA
    }

    public MercadoLivreTrackedItem(String itemId, String affiliateUrl) {
        this.itemId = itemId;
        this.affiliateUrl = affiliateUrl;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public String getItemId() {
        return itemId;
    }

    public String getAffiliateUrl() {
        return affiliateUrl;
    }

    public void setAffiliateUrl(String affiliateUrl) {
        this.affiliateUrl = affiliateUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
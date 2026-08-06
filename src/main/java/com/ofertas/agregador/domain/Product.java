package com.ofertas.agregador.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product", uniqueConstraints = {
        @UniqueConstraint(name = "uq_product_store_external", columnNames = {"store_id", "external_id"})
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "external_id", nullable = false, length = 150)
    private String externalId;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "affiliate_url", columnDefinition = "TEXT")
    private String affiliateUrl;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "list_price", precision = 10, scale = 2)
    private BigDecimal listPrice;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "coupon_description", length = 255)
    private String couponDescription;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Product() {
        // exigido pelo JPA
    }

    public Product(Store store, String externalId, String title, String originalUrl) {
        this.store = store;
        this.externalId = externalId;
        this.title = title;
        this.originalUrl = originalUrl;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Atualiza os dados variáveis do produto (preço, cupom, disponibilidade)
     * numa nova varredura do ProductFetcher, sem recriar a linha —
     * é o método que o OfferScannerJob deve chamar no fluxo de upsert.
     */
    public void updateFromScan(BigDecimal currentPrice, BigDecimal listPrice,
                                String couponCode, String couponDescription,
                                String affiliateUrl) {
        this.currentPrice = currentPrice;
        this.listPrice = listPrice;
        this.couponCode = couponCode;
        this.couponDescription = couponDescription;
        this.affiliateUrl = affiliateUrl;
        this.lastCheckedAt = LocalDateTime.now();
    }

    public boolean hasCoupon() {
        return couponCode != null && !couponCode.isBlank();
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getAffiliateUrl() {
        return affiliateUrl;
    }

    public void setAffiliateUrl(String affiliateUrl) {
        this.affiliateUrl = affiliateUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getListPrice() {
        return listPrice;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public String getCouponDescription() {
        return couponDescription;
    }

    public LocalDateTime getLastCheckedAt() {
        return lastCheckedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

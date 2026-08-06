package com.ofertas.agregador.store.magalu;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "magalu_tracked_product")
public class MagaluTrackedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_url", nullable = false, unique = true, columnDefinition = "TEXT")
    private String productUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected MagaluTrackedProduct() {
    }

    public MagaluTrackedProduct(String productUrl) {
        this.productUrl = productUrl;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getProductUrl() { return productUrl; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
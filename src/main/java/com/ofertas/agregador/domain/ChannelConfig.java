package com.ofertas.agregador.domain;

import com.ofertas.agregador.domain.enums.ChannelType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "channel_config", uniqueConstraints = {
        @UniqueConstraint(name = "uq_channel_destination", columnNames = {"channel_code", "destination_id"})
})
public class ChannelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_code", nullable = false, length = 30)
    private ChannelType channelCode;

    @Column(name = "destination_id", nullable = false, length = 150)
    private String destinationId;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "min_discount_pct", precision = 5, scale = 2)
    private BigDecimal minDiscountPct = BigDecimal.ZERO;

    @Column(name = "category_filter", length = 100)
    private String categoryFilter;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ChannelConfig() {
        // exigido pelo JPA
    }

    public ChannelConfig(ChannelType channelCode, String destinationId) {
        this.channelCode = channelCode;
        this.destinationId = destinationId;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Verifica se esta configuração de canal aceita a oferta,
     * considerando o filtro mínimo de desconto e a categoria (quando definida).
     * Centraliza a regra aqui em vez de espalhar comparações pelo OfferDispatcherJob.
     */
    public boolean accepts(BigDecimal discountPct, String productCategory) {
        boolean passesDiscount = discountPct != null
                && discountPct.compareTo(minDiscountPct) >= 0;

        boolean passesCategory = categoryFilter == null
                || categoryFilter.equalsIgnoreCase(productCategory);

        return active && passesDiscount && passesCategory;
    }

    public Long getId() {
        return id;
    }

    public ChannelType getChannelCode() {
        return channelCode;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BigDecimal getMinDiscountPct() {
        return minDiscountPct;
    }

    public void setMinDiscountPct(BigDecimal minDiscountPct) {
        this.minDiscountPct = minDiscountPct;
    }

    public String getCategoryFilter() {
        return categoryFilter;
    }

    public void setCategoryFilter(String categoryFilter) {
        this.categoryFilter = categoryFilter;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

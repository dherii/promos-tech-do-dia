package com.ofertas.agregador.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "offer_history")
public class OfferHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_pct", precision = 5, scale = 2)
    private BigDecimal discountPct;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "coupon_description", length = 255)
    private String couponDescription;

    @Column(name = "captured_at", nullable = false, updatable = false)
    private LocalDateTime capturedAt;

    protected OfferHistory() {
        // exigido pelo JPA
    }

    public OfferHistory(Product product, BigDecimal price, BigDecimal discountPct,
                         String couponCode, String couponDescription) {
        this.product = product;
        this.price = price;
        this.discountPct = discountPct;
        this.couponCode = couponCode;
        this.couponDescription = couponDescription;
    }

    @PrePersist
    protected void onCreate() {
        this.capturedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getDiscountPct() {
        return discountPct;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public String getCouponDescription() {
        return couponDescription;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }
}

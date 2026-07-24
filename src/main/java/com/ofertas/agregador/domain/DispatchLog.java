package com.ofertas.agregador.domain;

import com.ofertas.agregador.domain.enums.ChannelType;
import com.ofertas.agregador.domain.enums.DispatchStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispatch_log")
public class DispatchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_code", nullable = false, length = 30)
    private ChannelType channelCode;

    @Column(name = "price_at_dispatch", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtDispatch;

    @Column(name = "coupon_code_at_dispatch", length = 50)
    private String couponCodeAtDispatch;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DispatchStatus status;

    @Column(name = "dispatched_at", nullable = false, updatable = false)
    private LocalDateTime dispatchedAt;

    protected DispatchLog() {
        // exigido pelo JPA
    }

    public DispatchLog(Product product, ChannelType channelCode, BigDecimal priceAtDispatch,
                        String couponCodeAtDispatch, DispatchStatus status) {
        this.product = product;
        this.channelCode = channelCode;
        this.priceAtDispatch = priceAtDispatch;
        this.couponCodeAtDispatch = couponCodeAtDispatch;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        this.dispatchedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public ChannelType getChannelCode() {
        return channelCode;
    }

    public BigDecimal getPriceAtDispatch() {
        return priceAtDispatch;
    }

    public String getCouponCodeAtDispatch() {
        return couponCodeAtDispatch;
    }

    public DispatchStatus getStatus() {
        return status;
    }

    public LocalDateTime getDispatchedAt() {
        return dispatchedAt;
    }
}

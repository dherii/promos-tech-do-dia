package com.ofertas.agregador.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "platform_config", uniqueConstraints = {
        @UniqueConstraint(name = "uq_platform_config_key", columnNames = {"store_id", "config_key"})
})
public class PlatformConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PlatformConfig() {
        // exigido pelo JPA
    }

    public PlatformConfig(Store store, String configKey, String configValue) {
        this.store = store;
        this.configKey = configKey;
        this.configValue = configValue;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

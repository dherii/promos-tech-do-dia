package com.ofertas.agregador.domain;

import com.ofertas.agregador.domain.enums.IntegrationType;
import com.ofertas.agregador.domain.enums.StoreType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mapeado para o enum StoreType (em vez de String solta) para que o
     * banco e o código nunca fiquem dessincronizados sobre quais lojas existem.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true, length = 30)
    private StoreType code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_type", nullable = false, length = 20)
    private IntegrationType integrationType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Store() {
        // exigido pelo JPA
    }

    public Store(StoreType code, String name, IntegrationType integrationType) {
        this.code = code;
        this.name = name;
        this.integrationType = integrationType;
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

    public Long getId() {
        return id;
    }

    public StoreType getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

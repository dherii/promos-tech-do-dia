package com.ofertas.agregador.store.magalu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MagaluTrackedProductRepository extends JpaRepository<MagaluTrackedProduct, Long> {
    List<MagaluTrackedProduct> findByActiveTrue();
}
package com.ofertas.agregador.store.mercadolivre;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MercadoLivreTrackedItemRepository extends JpaRepository<MercadoLivreTrackedItem, String> {

    List<MercadoLivreTrackedItem> findByActiveTrue();
}
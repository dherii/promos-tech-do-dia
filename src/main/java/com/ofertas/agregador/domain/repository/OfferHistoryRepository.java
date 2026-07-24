package com.ofertas.agregador.domain.repository;

import com.ofertas.agregador.domain.OfferHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferHistoryRepository extends JpaRepository<OfferHistory, Long> {

    List<OfferHistory> findByProductIdOrderByCapturedAtDesc(Long productId);
}

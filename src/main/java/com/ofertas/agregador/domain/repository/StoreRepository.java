package com.ofertas.agregador.domain.repository;

import com.ofertas.agregador.domain.Store;
import com.ofertas.agregador.domain.enums.StoreType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByCode(StoreType code);

    List<Store> findByActiveTrue();
}

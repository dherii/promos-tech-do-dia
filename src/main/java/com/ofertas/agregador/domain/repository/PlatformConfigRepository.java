package com.ofertas.agregador.domain.repository;

import com.ofertas.agregador.domain.PlatformConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformConfigRepository extends JpaRepository<PlatformConfig, Long> {

    Optional<PlatformConfig> findByStoreIdAndConfigKey(Long storeId, String configKey);
}

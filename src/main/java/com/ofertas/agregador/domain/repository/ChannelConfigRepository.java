package com.ofertas.agregador.domain.repository;

import com.ofertas.agregador.domain.ChannelConfig;
import com.ofertas.agregador.domain.enums.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelConfigRepository extends JpaRepository<ChannelConfig, Long> {

    /**
     * Usado pelo OfferDispatcherJob para varrer todos os destinos ativos
     * de um canal (ex: todos os grupos do Telegram) a cada ciclo.
     */
    List<ChannelConfig> findByChannelCodeAndActiveTrue(ChannelType channelCode);
}

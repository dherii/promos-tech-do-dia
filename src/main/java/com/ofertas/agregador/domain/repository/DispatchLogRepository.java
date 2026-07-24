package com.ofertas.agregador.domain.repository;

import com.ofertas.agregador.domain.DispatchLog;
import com.ofertas.agregador.domain.enums.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface DispatchLogRepository extends JpaRepository<DispatchLog, Long> {

    /**
     * Espelha em código a mesma regra do índice único {@code uq_dispatch_dedup}
     * do banco (product_id, channel_code, price_at_dispatch, COALESCE(coupon_code_at_dispatch, '')).
     *
     * Usar COALESCE aqui é essencial: no Postgres, NULL nunca é igual a NULL,
     * então duas ofertas sem cupom seriam tratadas como "diferentes" sem essa
     * normalização — permitindo reenvio indevido. O OfferDispatcherJob deve
     * chamar este método ANTES de tentar enviar, como checagem rápida em memória;
     * a constraint do banco continua sendo a garantia final contra race conditions.
     */
    @Query("""
            SELECT COUNT(d) > 0 FROM DispatchLog d
            WHERE d.product.id = :productId
              AND d.channelCode = :channelCode
              AND d.priceAtDispatch = :price
              AND COALESCE(d.couponCodeAtDispatch, '') = COALESCE(:couponCode, '')
            """)
    boolean existsDuplicateDispatch(@Param("productId") Long productId,
                                     @Param("channelCode") ChannelType channelCode,
                                     @Param("price") BigDecimal price,
                                     @Param("couponCode") String couponCode);
}

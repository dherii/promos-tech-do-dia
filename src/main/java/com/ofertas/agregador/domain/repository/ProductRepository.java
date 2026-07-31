package com.ofertas.agregador.domain.repository;

import com.ofertas.agregador.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Usado pelo OfferScannerJob no fluxo de upsert: busca o produto existente
     * pela chave natural (loja + id externo) antes de decidir entre
     * atualizar ou criar uma nova linha.
     */
    Optional<Product> findByStoreIdAndExternalId(Long storeId, String externalId);

    /**
     * Usado pelo OfferDispatcherJob para selecionar apenas produtos
     * que mudaram desde o último ciclo de scan — evita reprocessar a tabela inteira
     * a cada execução.
     */
    List<Product> findByUpdatedAtAfter(LocalDateTime threshold);
}

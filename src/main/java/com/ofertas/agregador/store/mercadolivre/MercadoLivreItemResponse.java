package com.ofertas.agregador.store.mercadolivre;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Mapeia apenas os campos que usamos da resposta de {@code GET /items/{id}}.
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} é essencial aqui: a resposta
 * real do Mercado Livre tem dezenas de campos, e não queremos que o parsing quebre
 * se a API adicionar/remover campos que não usamos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoLivreItemResponse(
        String id,
        String title,
        BigDecimal price,
        BigDecimal original_price,
        String thumbnail,
        String permalink,
        String category_id
) {
}

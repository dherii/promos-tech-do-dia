package com.ofertas.agregador.store.aliexpress;

import java.math.BigDecimal;
import java.util.List;

public record AliexpressResponse(AliexpressAffiliateProductQueryResponse aliexpress_affiliate_product_query_response) {
    
    public record AliexpressAffiliateProductQueryResponse(RespResult resp_result) {}
    
    public record RespResult(Integer resp_code, Result result) {}
    
    public record Result(Products products) {}
    
    public record Products(List<Product> product) {}
    
    public record Product(
            String product_id,
            String product_title,
            String product_main_image_url,
            BigDecimal target_sale_price,
            BigDecimal target_original_price,
            String promotion_link
    ) {}
}
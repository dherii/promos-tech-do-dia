package com.ofertas.agregador.config;

import com.ofertas.agregador.store.shopee.ShopeeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ShopeeProperties.class)
public class ShopeeConfig {
}

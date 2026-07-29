package com.ofertas.agregador.config;

import com.ofertas.agregador.store.mercadolivre.MercadoLivreProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MercadoLivreProperties.class)
public class MercadoLivreConfig {
}

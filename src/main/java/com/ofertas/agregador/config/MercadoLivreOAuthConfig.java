package com.ofertas.agregador.config;

import com.ofertas.agregador.store.mercadolivre.MercadoLivreOAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MercadoLivreOAuthProperties.class)
public class MercadoLivreOAuthConfig {
}
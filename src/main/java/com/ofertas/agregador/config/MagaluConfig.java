package com.ofertas.agregador.config;

import com.ofertas.agregador.store.magalu.MagaluProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MagaluProperties.class)
public class MagaluConfig {
}

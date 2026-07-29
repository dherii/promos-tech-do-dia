package com.ofertas.agregador.config;

import com.ofertas.agregador.store.amazon.AmazonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AmazonProperties.class)
public class AmazonConfig {
}

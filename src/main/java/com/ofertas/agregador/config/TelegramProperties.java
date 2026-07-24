package com.ofertas.agregador.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramProperties {

    private String apiBaseUrl = "https://api.telegram.org";
    private String token;

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getBotToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
package com.ofertas.agregador.store.amazon;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Vincula {@code amazon.*} do application.properties.
 *
 * associateTag é tudo que o AmazonLinkGenerator precisa — não depende de nenhuma
 * API. Os demais campos são para quando (e se) o AmazonProductFetcher via
 * Creators API for habilitado (ver aviso na classe).
 */
@ConfigurationProperties(prefix = "amazon")
public class AmazonProperties {

    private String associateTag;
    private boolean creatorsApiEnabled = false;
    private String clientId;
    private String clientSecret;

    public String getAssociateTag() {
        return associateTag;
    }

    public void setAssociateTag(String associateTag) {
        this.associateTag = associateTag;
    }

    public boolean isCreatorsApiEnabled() {
        return creatorsApiEnabled;
    }

    public void setCreatorsApiEnabled(boolean creatorsApiEnabled) {
        this.creatorsApiEnabled = creatorsApiEnabled;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}

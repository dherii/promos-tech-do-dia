package com.ofertas.agregador.store.shopee;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Vincula {@code shopee.*} do application.properties.
 *
 * Exemplo application.properties:
 * <pre>
 * shopee.app-id=${SHOPEE_APP_ID}
 * shopee.secret=${SHOPEE_SECRET}
 * shopee.api-base-url=https://open-api.affiliate.shopee.com.br/graphql
 * shopee.list-type=0
 * shopee.sort-type=2
 * shopee.page-limit=50
 * </pre>
 */
@ConfigurationProperties(prefix = "shopee")
public class ShopeeProperties {

    private String appId;
    private String secret;
    private String apiBaseUrl = "https://open-api.affiliate.shopee.com.br/graphql";
    private int listType = 0;
    private int sortType = 2;
    private int pageLimit = 50;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public int getListType() {
        return listType;
    }

    public void setListType(int listType) {
        this.listType = listType;
    }

    public int getSortType() {
        return sortType;
    }

    public void setSortType(int sortType) {
        this.sortType = sortType;
    }

    public int getPageLimit() {
        return pageLimit;
    }

    public void setPageLimit(int pageLimit) {
        this.pageLimit = pageLimit;
    }
}

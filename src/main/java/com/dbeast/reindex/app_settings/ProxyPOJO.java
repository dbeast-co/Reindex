package com.dbeast.reindex.app_settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;


public class ProxyPOJO {
    private static final Logger logger = LogManager.getLogger();

    private boolean isProxyConfigured = false;
    private String proxyHTTPHost;
    private int proxyHTTPPort = 8080;
    private String proxyHTTPUser = "";
    private String proxyHTTPPassword = "";
    private boolean proxyHTTPHasAuth = false;
    private boolean isNoProxyConfigured;
    private List<String> noProxyList;

    public ProxyPOJO(AppSettingsPOJO appSettings) {
        if (appSettings == null || appSettings.getApp() == null) {
            logger.warn("App settings are not properly configured - proxy settings will be ignored");
            return;
        }

        String proxyHost = appSettings.getApp().getHttp_proxy();

        if (proxyHost == null || proxyHost.isEmpty() || proxyHost.equalsIgnoreCase("null")) {
            this.isProxyConfigured = false;
            return;
        } else {
            this.isProxyConfigured = true;
        }

        if (proxyHost.contains(":")) {
            String[] parts = proxyHost.split(":");
            this.proxyHTTPHost = parts[0];
            try {
                this.proxyHTTPPort = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid proxy port in configuration: {}", parts[1]);
                return;
            }
        }

        this.proxyHTTPUser = appSettings.getApp().getProxy_user();
        this.proxyHTTPPassword = appSettings.getApp().getProxy_password();
        this.proxyHTTPHasAuth = this.proxyHTTPUser != null && !this.proxyHTTPUser.isEmpty() && !this.proxyHTTPUser.equalsIgnoreCase("null")
                && this.proxyHTTPPassword != null && !this.proxyHTTPPassword.isEmpty() && !this.proxyHTTPPassword.equalsIgnoreCase("null");

        logger.debug("Proxy configured: {}:{}, auth: {}", this.proxyHTTPHost, this.proxyHTTPPort,  this.proxyHTTPHasAuth);

        String noProxy = appSettings.getApp().getNo_proxy();

        if (noProxy != null && !noProxy.isEmpty() && !noProxy.equalsIgnoreCase("null")) {
            this.isNoProxyConfigured = true;

            this.noProxyList = Arrays.asList(noProxy.split(","));
        }
    }

    public boolean isHostInNonProxyList(final String host) {
        if (isNoProxyConfigured) {
            return noProxyList.stream()
                    .anyMatch(noProxyHost -> host.toLowerCase().contains(noProxyHost.toLowerCase()));
        } else {
            return false;
        }
    }

    public boolean isProxyConfigured() {
        return isProxyConfigured;
    }

    public String getProxyHTTPHost() {
        return proxyHTTPHost;
    }

    public int getProxyHTTPPort() {
        return proxyHTTPPort;
    }

    public String getProxyHTTPUser() {
        return proxyHTTPUser;
    }

    public String getProxyHTTPPassword() {
        return proxyHTTPPassword;
    }

    public boolean isProxyHTTPHasAuth() {
        return proxyHTTPHasAuth;
    }

    public boolean isNoProxyConfigured() {
        return isNoProxyConfigured;
    }

    public List<String> getNoProxyList() {
        return noProxyList;
    }
}

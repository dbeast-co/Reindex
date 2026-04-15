package com.dbeast.reindex.elasticsearch;

import com.dbeast.reindex.app_settings.AppSettingsPOJO;
import com.dbeast.reindex.app_settings.ProxyPOJO;
import com.dbeast.reindex.data_warehouse.DataWarehouse;
import com.dbeast.reindex.project_settings.ESHostPOJO;
import com.dbeast.reindex.project_settings.EsSettingsPOJO;
import com.dbeast.reindex.exceptions.ClusterConnectionException;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import static com.dbeast.reindex.Reindex.FILE_SEPARATOR;

public class ElasticsearchDbProvider {
    private static final Logger logger = LogManager.getLogger();
    private final String projectsFolder;
    private final AppSettingsPOJO appSettings = DataWarehouse.getInstance().getAppSettings();
    private final ProxyPOJO proxySettings = new ProxyPOJO(appSettings);

    public ElasticsearchDbProvider() {
        projectsFolder = DataWarehouse.getInstance().getAppSettings().getInternals().getProjectsFolder();
    }

    public RestHighLevelClient getHighLevelClient(final EsSettingsPOJO connectionSettings,
                                                  final String projectId) throws ClusterConnectionException {
        RestClientBuilder clientBuilder = buildLowLevelRestClient(connectionSettings);
        if (connectionSettings.isSsl_enabled() && connectionSettings.isAuthentication_enabled()) {
            addSslToClientBuilder(connectionSettings, clientBuilder, projectId);
        } else if (connectionSettings.isAuthentication_enabled()) {
            addBasicAuthenticationToClientBuilder(connectionSettings, clientBuilder);
        } else {
            addProxyOnlyToClientBuilder(clientBuilder, connectionSettings);
        }
        return new RestHighLevelClient(clientBuilder);
    }


    public RestHighLevelClient getHighLevelClient(final EsSettingsPOJO connectionSettings) throws ClusterConnectionException {
        RestClientBuilder clientBuilder = buildLowLevelRestClient(connectionSettings);
        if (connectionSettings.isSsl_enabled() && connectionSettings.isAuthentication_enabled()) {
            addSslToClientBuilder(connectionSettings, clientBuilder, connectionSettings.getSsl_file());
        } else if (connectionSettings.isAuthentication_enabled()) {
            addBasicAuthenticationToClientBuilder(connectionSettings, clientBuilder);
        } else {
            addProxyOnlyToClientBuilder(clientBuilder, connectionSettings);
        }
        return new RestHighLevelClient(clientBuilder);
    }

    private void addProxyOnlyToClientBuilder(final RestClientBuilder clientBuilder, EsSettingsPOJO connectionSettings) {
        if (proxySettings.isProxyConfigured() && !proxySettings.isHostInNonProxyList(connectionSettings.getEs_host())) {
            clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    applyProxy(httpClientBuilder, proxySettings, credentialsProvider);
                    return httpClientBuilder;
                }
            });
        }
    }


    public RestClient getLowLevelClient(final EsSettingsPOJO connectionSettings,
                                        final String projectId) throws ClusterConnectionException {
        return getHighLevelClient(connectionSettings, projectId).getLowLevelClient();
    }

    private RestClientBuilder buildLowLevelRestClient(final EsSettingsPOJO connectionSettings) {
        ESHostPOJO esHost = new ESHostPOJO(connectionSettings);

        return RestClient.builder(new HttpHost(
                esHost.getDomain(),
                esHost.getPort(),
                esHost.getProtocol()));
    }

    private void addSslToClientBuilder(final EsSettingsPOJO connectionSettings,
                                       final RestClientBuilder clientBuilder,
                                       String projectId) throws ClusterConnectionException {
        if (connectionSettings.getSsl_file() != null) {
            try {
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(connectionSettings.getUsername(), connectionSettings.getPassword()));
                Path caCertificatePath = Paths.get(projectsFolder + projectId + FILE_SEPARATOR + connectionSettings.getSsl_file());
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                Certificate trustedCa;
                try (InputStream is = Files.newInputStream(caCertificatePath)) {
                    trustedCa = factory.generateCertificate(is);
                }
                KeyStore trustStore = KeyStore.getInstance("pkcs12");
                trustStore.load(null, null);
                trustStore.setCertificateEntry("ca", trustedCa);
                SSLContextBuilder sslContextBuilder = SSLContexts.custom()
                        .loadTrustMaterial(trustStore, null);
                final SSLContext sslContext = sslContextBuilder.build();

                clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(
                            HttpAsyncClientBuilder httpClientBuilder) {

                        httpClientBuilder.setSSLContext(sslContext)
                                .setSSLHostnameVerifier((s, sslSession) -> true);

                        // Merge credentials: ES + proxy (if needed)
                        if (proxySettings.isProxyConfigured() && !proxySettings.isHostInNonProxyList(connectionSettings.getEs_host())) {
                            applyProxy(httpClientBuilder, proxySettings, credentialsProvider);
                        } else {
                            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                        }

                        return httpClientBuilder;
                    }
                });
            } catch (NoSuchAlgorithmException | KeyManagementException | IOException | CertificateException |
                     KeyStoreException e) {
                logger.warn("error in the connection to the cluster\n" + e);
                throw new ClusterConnectionException(e.getMessage(), e);
            }
        } else {
            addSslToClientBuilder(connectionSettings, clientBuilder);
        }
    }


    /**
     * Applies proxy settings to the HTTP client builder, merging proxy credentials
     * with any existing ES credentials into a single CredentialsProvider.
     */
    private void applyProxy(HttpAsyncClientBuilder httpClientBuilder,
                            ProxyPOJO proxyConfig,
                            CredentialsProvider existingCredentials) {

        HttpHost proxy = new HttpHost(proxyConfig.getProxyHTTPHost(),
                proxyConfig.getProxyHTTPPort(),
                "http");
        httpClientBuilder.setRoutePlanner(new DefaultProxyRoutePlanner(proxy));
        logger.info("Proxy route planner configured for {}:{}",
                proxyConfig.getProxyHTTPHost(),
                proxyConfig.getProxyHTTPPort());

        // Use existing credentials provider (which may already contain ES auth)
        // and add proxy credentials to it
        if (proxyConfig.isProxyHTTPHasAuth()) {
            existingCredentials.setCredentials(
                    new AuthScope( proxyConfig.getProxyHTTPHost(),
                            proxyConfig.getProxyHTTPPort()),
                    new UsernamePasswordCredentials(proxyConfig.getProxyHTTPUser(),
                            proxyConfig.getProxyHTTPPassword())
            );
            logger.info("Proxy authentication configured for user: {}",
                    proxyConfig.getProxyHTTPUser());
        }
        httpClientBuilder.setDefaultCredentialsProvider(existingCredentials);
    }


    private void addSslToClientBuilder(final EsSettingsPOJO connectionSettings,
                                       final RestClientBuilder clientBuilder) throws ClusterConnectionException {
        try {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(connectionSettings.getUsername(), connectionSettings.getPassword()));
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{UnsafeX509ExtendedTrustManager.INSTANCE}, null);

            clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(
                        HttpAsyncClientBuilder httpClientBuilder) {

                    httpClientBuilder.setSSLContext(sslContext)
                            .setSSLHostnameVerifier((s, sslSession) -> true);

                    // Merge credentials: ES + proxy (if needed)
                    if (proxySettings.isProxyConfigured() && !proxySettings.isHostInNonProxyList(connectionSettings.getEs_host())) {
                        applyProxy(httpClientBuilder, proxySettings, credentialsProvider);
                    } else {
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }

                    return httpClientBuilder;
                }
            });
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.warn("error in the connection to the cluster\n" + e);
            throw new ClusterConnectionException(e.getMessage(), e);
        }
    }

    private void addBasicAuthenticationToClientBuilder(final EsSettingsPOJO connectionSettings,
                                                       final RestClientBuilder clientBuilder) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(connectionSettings.getUsername(), connectionSettings.getPassword()));

        clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                if (proxySettings.isProxyConfigured() && !proxySettings.isHostInNonProxyList(connectionSettings.getEs_host())) {
                    applyProxy(httpClientBuilder, proxySettings, credentialsProvider);
                } else {
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
                return httpClientBuilder;
            }
        });
    }

}




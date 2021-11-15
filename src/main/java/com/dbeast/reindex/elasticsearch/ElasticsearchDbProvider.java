package com.dbeast.reindex.elasticsearch;

import com.dbeast.reindex.data_warehouse.DataWarehouse;
import com.dbeast.reindex.project_settings.ESHostPOJO;
import com.dbeast.reindex.project_settings.EsSettings;
import com.dbeast.reindex.exceptions.ClusterConnectionException;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
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

    public ElasticsearchDbProvider() {
        projectsFolder = DataWarehouse.getInstance().getAppSettings().getInternals().getProjectsFolder();
    }

    public RestHighLevelClient getHighLevelClient(final EsSettings connectionSettings,
                                                  final String projectId) throws ClusterConnectionException {
        RestClientBuilder clientBuilder = buildLowLevelRestClient(connectionSettings);
        if (connectionSettings.isSsl_enabled() && connectionSettings.isAuthentication_enabled()) {
            clientBuilder = addSslToClientBuilder(connectionSettings, clientBuilder, projectId);
        } else if (connectionSettings.isAuthentication_enabled()) {
            clientBuilder = addBasicAuthenticationToClientBuilder(connectionSettings, clientBuilder);
        }
        return new RestHighLevelClient(clientBuilder);
    }

    public RestClient getLowLevelClient(final EsSettings connectionSettings,
                                        final String projectId) throws ClusterConnectionException {
        return getHighLevelClient(connectionSettings, projectId).getLowLevelClient();
    }

    private RestClientBuilder buildLowLevelRestClient(final EsSettings connectionSettings) {
        ESHostPOJO esHost = new ESHostPOJO(connectionSettings);
        return RestClient.builder(new HttpHost(
                esHost.getDomain(),
                esHost.getPort(),
                esHost.getProtocol()));
    }

    private RestClientBuilder addSslToClientBuilder(final EsSettings connectionSettings,
                                                    final RestClientBuilder clientBuilder,
                                                    final String projectId) throws ClusterConnectionException {
        if (connectionSettings.getSsl_file() != null) {
            try {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
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
                        return httpClientBuilder.setSSLContext(sslContext)
                                .setDefaultCredentialsProvider(credentialsProvider)
                                .setSSLHostnameVerifier((s, sslSession) -> true);
                    }
                });
                return clientBuilder;
            } catch (NoSuchAlgorithmException | KeyManagementException | IOException | CertificateException | KeyStoreException e) {
                logger.warn("error in the connection to the cluster\n" + e);
                throw new ClusterConnectionException(e.getMessage());
            }
        } else {
            return addSslToClientBuilder(connectionSettings, clientBuilder);
        }
    }

    private RestClientBuilder addSslToClientBuilder(final EsSettings connectionSettings,
                                                    final RestClientBuilder clientBuilder) throws ClusterConnectionException {
        try {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(connectionSettings.getUsername(), connectionSettings.getPassword()));
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{UnsafeX509ExtendedTrustManager.INSTANCE}, null);

            clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(
                        HttpAsyncClientBuilder httpClientBuilder) {
                    return httpClientBuilder.setSSLContext(sslContext)
                            .setDefaultCredentialsProvider(credentialsProvider)
                            //This supposed to disable certificate verification
                            .setSSLHostnameVerifier((s, sslSession) -> true);
                }
            });
            return clientBuilder;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.warn("error in the connection to the cluster\n" + e);
            throw new ClusterConnectionException(e.getMessage());
        }
    }

    private RestClientBuilder addBasicAuthenticationToClientBuilder(final EsSettings connectionSettings,
                                                                    final RestClientBuilder clientBuilder) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(connectionSettings.getUsername(), connectionSettings.getPassword()));
        clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(
                    HttpAsyncClientBuilder httpClientBuilder) {
                return httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider);
            }
        });
        return clientBuilder;
    }
}




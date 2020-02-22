package stoner.tresttemplate.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Component
@Slf4j
public class RestTemplateConfig {

    //Determines the timeout in milliseconds until a  connection is established.
    private static final int CONNECT_TIMEOUT = 30000;

    //The  timeout when requesting a connection from the connection manager.
    private static final int REQUEST_TIMEOUT = 30000;

    //The timeout for waiting for data
    private static final int SOCKET_TIMEOUT = 60000;

    private static final int DEFAULT_KEEP_ALIVE_TIME_MIlLIS = 20000;

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory httpComponentsClientHttpRequestfactory) {
        RestTemplate restTemplate = new RestTemplate(httpComponentsClientHttpRequestfactory);
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> converter : messageConverters) {
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8);
            }
        }
        return restTemplate;
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestfactory(HttpClient httpClient){
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    @Bean
    public CloseableHttpClient httpClient(RequestConfig requestConfig, PoolingHttpClientConnectionManager poolingConnectionManager, ConnectionKeepAliveStrategy connectionKeepAliveStrategy) {
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager)
                .setKeepAliveStrategy(connectionKeepAliveStrategy)
                .build();
    }

    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom ()
                .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                .setConnectTimeout (CONNECT_TIMEOUT)
                .setSocketTimeout (SOCKET_TIMEOUT).build();
    }

    @Bean
    public PoolingHttpClientConnectionManager poolingConnectionManager() {
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        }catch (NoSuchAlgorithmException | KeyStoreException e) {
            log.error ("Pooling Connection Manager Initialisation failure because of"+ e.getMessage(), e);
        }
        SSLConnectionSocketFactory sslsf = null;
        try{
            sslsf = new SSLConnectionSocketFactory (builder.build());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            log.error("Pooling Connection Manager Initialisation failure because of" + e.getMessage(), e);
        }
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslsf)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingConnectionManager.setMaxTotal(getMaxCpuCore() * 2 + 3);
        poolingConnectionManager.setDefaultMaxPerRoute(getMaxCpuCore() * 2);
        return poolingConnectionManager;
    }

    @Bean
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return (response, context) -> {
            HeaderElementIterator it = new BasicHeaderElementIterator
                    (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return DEFAULT_KEEP_ALIVE_TIME_MIlLIS;
        };
    }

    private static int getMaxCpuCore(){
        return Runtime.getRuntime().availableProcessors();
    }

}

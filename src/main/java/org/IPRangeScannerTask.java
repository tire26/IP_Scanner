package org;

import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.SSLInitializationException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

class IPRangeScannerTask implements Runnable {
    private final List<String> ips;

    public IPRangeScannerTask(List<String> ips) {
        this.ips = ips;
    }

    @Override
    public void run() {
        Map<String, String> domainNames = new HashMap<>();
        for (String ip : ips) {
            Set<String> scannedDomains = scanSSLDomains(ip);
            if (scannedDomains != null) {
                scannedDomains.stream().forEach(sd -> {
                    domainNames.put(ip, sd);
                });
            }
        }
        FileUtil.saveDomainNamesToFile(domainNames);
    }

    public static final String PEER_CERTIFICATES = "PEER_CERTIFICATES";

    public static Set<String> scanSSLDomains(String ipAddress) {
        try {

            HttpResponseInterceptor certificateInterceptor = (httpResponse, context) -> {
                ManagedHttpClientConnection routedConnection = (ManagedHttpClientConnection)context.getAttribute(HttpCoreContext.HTTP_CONNECTION);
                SSLSession sslSession = routedConnection.getSSLSession();
                if (sslSession != null) {
                    Certificate[] certificates = sslSession.getPeerCertificates();
                    context.setAttribute(PEER_CERTIFICATES, certificates);
                }
            };

            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .build();
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                    .addInterceptorFirst(certificateInterceptor)
                    .build();

            String url = "https://" + ipAddress;
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(RequestConfig.custom()
                    .setConnectTimeout(1000)
                    .setSocketTimeout(1000)
                    .setConnectionRequestTimeout(1000)
                    .build());
            HttpContext context = new BasicHttpContext();
            httpClient.execute(httpGet, context);

            Certificate[] peerCertificates = (Certificate[]) context.getAttribute(PEER_CERTIFICATES);
            Set<String> domain = new HashSet<>();
            if (peerCertificates != null) {
                for (Certificate peerCertificate : peerCertificates) {
                    X509Certificate x509Certificate = (X509Certificate) peerCertificate;
                    Collection<List<?>> subjectAlternativeNames = x509Certificate.getSubjectAlternativeNames();
                    if (subjectAlternativeNames != null) {
                        for (List<?> subjectAlternativeName : subjectAlternativeNames) {
                            if (subjectAlternativeName.get(0).equals(2)) {
                                domain.add(subjectAlternativeName.get(1).toString());
                                System.out.println("Domain: " + subjectAlternativeName.get(1));
                            }
                        }
                    }
                }
            }
            return domain;
        } catch (SSLInitializationException e) {
            System.err.println("Ошибка инициализации SSL: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка при сканировании SSL: " + e.getMessage());
        }
        return null;
    }

}

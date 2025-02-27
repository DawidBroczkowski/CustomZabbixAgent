package org.example;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class CertificateManager
{
    private final SSLContext sslContext;
    private final ZabbixConfig config;
    private X509Certificate[] pinnedCerts = null;
    private KeyManager[] keyManagers = null;
    private String thumbprint = null;


    public CertificateManager(ZabbixConfig config)
    {
        this.config = config;
        // Initialize SSLContext with default protocols
        try
        {
            this.sslContext = SSLContext.getInstance(config.getSslProtocols());
            // If no certificates provided, initialize SSLContext with defaults
            if (config.getClientCertificatePath() == null && config.getServerCertificateThumbprint() == null)
            {
                setUpSSLContext();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to initialize SSLContext", e);
        }
    }

    public void loadClientCertificate(String certFilePath, String keyFilePath, String password) throws Exception
    {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream keyFileStream = new FileInputStream(keyFilePath))
        {
            keyStore.load(keyFileStream, password.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password.toCharArray());
        KeyManager[] keyManagers = kmf.getKeyManagers();
        this.keyManagers = keyManagers;
        // Initialize SSLContext with client KeyManagers and default TrustManagers
        setUpSSLContext();
    }

    public void loadServerCertificate(String certFilePath) throws Exception
    {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate serverCert;
        try (FileInputStream certFileStream = new FileInputStream(certFilePath))
        {
            serverCert = cf.generateCertificate(certFileStream);
        }
        this.pinnedCerts = new X509Certificate[]{(X509Certificate) serverCert};
        // Set up SSLContext with the server certificate
        setUpSSLContext();
    }

    public void loadServerCertificateByThumbprint(String thumbprint) throws Exception
    {
        // Set up SSLContext with thumbprint validation
        this.thumbprint = thumbprint;
        setUpSSLContext();
    }

    private void setUpSSLContext() throws Exception
    {
        System.out.println("Initializing SSLContext with provided certificates and thumbprint.");
        TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager()
                {
                    public X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType)
                    {
                        // No client certificate validation
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType)
                    {
                        try
                        {
                            if (config.isPinCertificateBeforeCA())
                            {
                                if (pinnedCerts != null)
                                {
                                    for (X509Certificate cert : certs)
                                    {
                                        boolean trusted = false;
                                        for (X509Certificate pinnedCert : pinnedCerts)
                                        {
                                            try
                                            {
                                                cert.verify(pinnedCert.getPublicKey());
                                                trusted = true;
                                                break;
                                            }
                                            catch (Exception e)
                                            {
                                                // Continue checking other pinned certs
                                            }
                                        }
                                        if (trusted)
                                        {
                                            return;
                                        }
                                    }
                                    throw new RuntimeException("Server certificate verification failed");
                                }
                                else if (thumbprint != null)
                                {
                                    for (X509Certificate cert : certs)
                                    {
                                        try
                                        {
                                            String certThumbprint = getThumbprint(cert);
                                            if (thumbprint.equalsIgnoreCase(certThumbprint))
                                            {
                                                return;
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            throw new RuntimeException("Server certificate thumbprint verification failed", e);
                                        }
                                    }
                                    throw new RuntimeException("Server certificate thumbprint verification failed");
                                }
                                else
                                {
                                    throw new RuntimeException("Server certificate verification failed");
                                }
                            }

                            if (config.isPinCertificateAfterCA())
                            {
                                if (thumbprint != null)
                                {
                                    String expectedThumbprint = thumbprint.replace(" ", "").toUpperCase();
                                    for (X509Certificate cert : certs)
                                    {
                                        try
                                        {
                                            String actualThumbprint = getThumbprint(cert).toUpperCase();
                                            if (expectedThumbprint.equals(actualThumbprint))
                                            {
                                                return;
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            throw new RuntimeException("Server certificate thumbprint verification failed", e);
                                        }
                                    }
                                    throw new RuntimeException("Server certificate thumbprint verification failed");
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException("Exception during certificate validation", e);
                        }
                    }

                    private String getThumbprint(X509Certificate cert) throws Exception
                    {
                        try
                        {
                            MessageDigest md = MessageDigest.getInstance("SHA-256");
                            byte[] der = cert.getEncoded();
                            md.update(der);
                            byte[] digest = md.digest();
                            return Base64.getEncoder().encodeToString(digest).replaceAll("\\s", "");
                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException("Failed to compute certificate thumbprint", e);
                        }
                    }
                }
        };

        try
        {
            this.sslContext.init(keyManagers, trustManagers, new java.security.SecureRandom());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to initialize SSLContext with KeyManagers and TrustManagers", e);
        }
        System.out.println("SSLContext initialized successfully.");
    }

    public SSLSocketFactory getSocketFactory()
    {
        if (this.sslContext == null)
        {
            throw new IllegalStateException("SSLContext is not initialized");
        }
        if (this.sslContext.getSocketFactory() == null)
        {
            throw new IllegalStateException("SSLContext is initialized but SSLSocketFactory is null");
        }
        return this.sslContext.getSocketFactory();
    }

}


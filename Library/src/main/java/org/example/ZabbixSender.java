package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class ZabbixSender
{

    private final ZabbixConfig config;
    private final ObjectMapper objectMapper;
    private final CertificateManager certificateManager;
    private final byte[] header = new byte[]{0x5A, 0x42, 0x58, 0x44, 0x01}; // "ZBXD\1"
    private final byte[] zeroPadding = new byte[4]; // Zero padding for constructPacket
    private static final Logger logger = Logger.getLogger(ZabbixSender.class.getName());

    public ZabbixSender(ZabbixConfig config)
    {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.certificateManager = new CertificateManager(config);

        try
        {
            if (config.getClientCertificatePath() != null && config.getClientCertificatePassword() != null)
            {
                certificateManager.loadClientCertificate(
                        config.getClientCertificatePath(),
                        config.getClientCertificatePath(),
                        config.getClientCertificatePassword()
                );
            }
            if (config.getServerCertificatePath() != null)
            {
                certificateManager.loadServerCertificate(config.getServerCertificatePath());
            }
            else if (config.getServerCertificateThumbprint() != null)
            {
                certificateManager.loadServerCertificateByThumbprint(config.getServerCertificateThumbprint());
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error initializing CertificateManager", e);
        }
    }

    public CompletableFuture<String> sendAsync(String key, String value)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try
            {
                logger.fine("Key: " + key + " | Value: " + value);
                return send(key, value);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<String> sendItemsAsync(List<ZabbixTrapperItem> items)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try
            {
                //System.out.println("Sending multiple items");
                return sendItems(items);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    private String send(String key, String value) throws Exception
    {
        byte[] data = constructData(config.getHostName(), key, value);
        byte[] packet = constructPacket(data);

        try (Socket socket = new Socket())
        {
            socket.connect(new InetSocketAddress(config.getServerAddress(), config.getPort()), 3000);

            if (config.isUseEncryption())
            {
                return sendEncrypted(socket, packet);
            }
            else
            {
                return sendPlain(socket, packet);
            }
        }
        finally
        {
            // Nullify the arrays to help GC
            Arrays.fill(data, (byte) 0);
            Arrays.fill(packet, (byte) 0);
        }
    }

    private String sendItems(List<ZabbixTrapperItem> items) throws Exception
    {
        byte[] data = constructData(config.getHostName(), items);
        byte[] packet = constructPacket(data);

        try (Socket socket = new Socket())
        {
            socket.connect(new InetSocketAddress(config.getServerAddress(), config.getPort()), 3000);

            if (config.isUseEncryption())
            {
                return sendEncrypted(socket, packet);
            }
            else
            {
                return sendPlain(socket, packet);
            }
        }
        finally
        {
            // Nullify the arrays to help GC
            Arrays.fill(data, (byte) 0);
            Arrays.fill(packet, (byte) 0);
        }
    }

    private byte[] constructData(String host, String key, String value)
    {
        Map<String, Object> payload = new HashMap<>();
        payload.put("request", "sender data");
        Map<String, String> data = new HashMap<>();
        data.put("host", host);
        data.put("key", key);
        data.put("value", value);
        payload.put("data", new Map[]{data});

        try
        {
            return objectMapper.writeValueAsBytes(payload);
        }
        catch (Exception e)
        {
            logger.severe("Exception thrown in ZabbixSender: " + e.getMessage());
            logger.fine(e.getStackTrace().toString());
        }
        return new byte[0];
    }

    private byte[] constructData(String host, List<ZabbixTrapperItem> items)
    {
        Map<String, Object> payload = new HashMap<>();
        payload.put("request", "sender data");
        Map<String, String>[] data = new HashMap[items.size()];
        for (int i = 0; i < items.size(); i++)
        {
            ZabbixTrapperItem item = items.get(i);
            Map<String, String> dataItem = new HashMap<>();
            dataItem.put("host", host);
            dataItem.put("key", item.getKey());
            dataItem.put("value", item.getValue());
            data[i] = dataItem;
        }
        payload.put("data", data);

        try
        {
            return objectMapper.writeValueAsBytes(payload);
        }
        catch (Exception e)
        {
            logger.severe("Exception thrown in ZabbixSender: " + e.getMessage());
            logger.fine(e.getStackTrace().toString());
        }
        return new byte[0];
    }

    private byte[] constructPacket(byte[] data)
    {
        byte[] length = intToBytesLittleEndian(data.length);
        byte[] packet = new byte[header.length + 8 + data.length];
        System.arraycopy(header, 0, packet, 0, header.length);
        System.arraycopy(length, 0, packet, header.length, 4); // First 4 bytes of length
        System.arraycopy(zeroPadding, 0, packet, header.length + 4, 4); // Zero padding
        System.arraycopy(data, 0, packet, header.length + 8, data.length);

        return packet;
    }

    private String validateResponse(byte[] response, int bytesRead)
    {
        if (bytesRead < 13)
        {
            return null;
        }

        byte[] responseHeader = new byte[5];
        System.arraycopy(response, 0, responseHeader, 0, 5);

        if (!Arrays.equals(responseHeader, header))
        {
            return null;
        }

        return new String(response, 13, bytesRead - 13, StandardCharsets.UTF_8);
    }

    private String sendEncrypted(Socket socket, byte[] packet) throws Exception
    {
        SSLSocketFactory sslSocketFactory = certificateManager.getSocketFactory();
        if (sslSocketFactory == null)
        {
            throw new Exception("Failed to create SSL socket factory");
        }

        try (SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, config.getServerAddress(), config.getPort(), true))
        {
            sslSocket.startHandshake();

            sslSocket.getOutputStream().write(packet);

            byte[] response = new byte[4096];
            int bytesRead = sslSocket.getInputStream().read(response);
            return validateResponse(response, bytesRead);
        }
    }

    private String sendPlain(Socket socket, byte[] packet) throws Exception
    {
        socket.getOutputStream().write(packet);

        byte[] response = new byte[4096];
        int bytesRead = socket.getInputStream().read(response);
        return validateResponse(response, bytesRead);
    }

    private byte[] intToBytesLittleEndian(int value)
    {
        return new byte[]{
                (byte) value,
                (byte) (value >> 8),
                (byte) (value >> 16),
                (byte) (value >> 24)};
    }
}

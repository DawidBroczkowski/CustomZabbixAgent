package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;

public class ZabbixConfig
{
    private String serverAddress = "localhost";
    private int port = 10051;
    private String hostName = "";
    private boolean useEncryption = false;
    private String targetHost = "localhost";
    private String serverCertificateThumbprint;
    private String serverCertificatePath;
    private String clientCertificateThumbprint;
    private String clientCertificatePath;
    private String clientCertificatePassword;
    private String sslProtocols = "TLSv1.3";
    private boolean pinCertificateBeforeCA = false;
    private boolean pinCertificateAfterCA = false;

    public String getServerAddress()
    {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress)
    {
        this.serverAddress = serverAddress;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getHostName()
    {
        return hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public boolean isUseEncryption()
    {
        return useEncryption;
    }

    public void setUseEncryption(boolean useEncryption)
    {
        this.useEncryption = useEncryption;
    }

    public String getTargetHost()
    {
        return targetHost;
    }

    public void setTargetHost(String targetHost)
    {
        this.targetHost = targetHost;
    }

    public String getServerCertificateThumbprint()
    {
        return serverCertificateThumbprint;
    }

    public void setServerCertificateThumbprint(String serverCertificateThumbprint)
    {
        this.serverCertificateThumbprint = serverCertificateThumbprint;
    }

    public String getServerCertificatePath()
    {
        return serverCertificatePath;
    }

    public void setServerCertificatePath(String serverCertificatePath)
    {
        this.serverCertificatePath = serverCertificatePath;
    }

    public String getClientCertificateThumbprint()
    {
        return clientCertificateThumbprint;
    }

    public void setClientCertificateThumbprint(String clientCertificateThumbprint)
    {
        this.clientCertificateThumbprint = clientCertificateThumbprint;
    }

    public String getClientCertificatePath()
    {
        return clientCertificatePath;
    }

    public void setClientCertificatePath(String clientCertificatePath)
    {
        this.clientCertificatePath = clientCertificatePath;
    }

    public String getClientCertificatePassword()
    {
        return clientCertificatePassword;
    }

    public void setClientCertificatePassword(String clientCertificatePassword)
    {
        this.clientCertificatePassword = clientCertificatePassword;
    }

    public String getSslProtocols()
    {
        return sslProtocols;
    }

    public void setSslProtocols(String sslProtocols)
    {
        this.sslProtocols = sslProtocols;
    }

    public boolean isPinCertificateBeforeCA()
    {
        return pinCertificateBeforeCA;
    }

    public void setPinCertificateBeforeCA(boolean pinCertificateBeforeCA)
    {
        this.pinCertificateBeforeCA = pinCertificateBeforeCA;
    }

    public boolean isPinCertificateAfterCA()
    {
        return pinCertificateAfterCA;
    }

    public void setPinCertificateAfterCA(boolean pinCertificateAfterCA)
    {
        this.pinCertificateAfterCA = pinCertificateAfterCA;
    }

    public void saveConfig(String filePath)
    {
        ObjectMapper mapper = new ObjectMapper();
        try (FileWriter writer = new FileWriter(filePath))
        {
            mapper.writeValue(writer, this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void loadConfig(String filePath)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            File file = new File(filePath);
            if (file.exists())
            {
                ZabbixConfig config = mapper.readValue(file, ZabbixConfig.class);
                this.serverAddress = config.serverAddress;
                this.port = config.port;
                this.hostName = config.hostName;
                this.useEncryption = config.useEncryption;
                this.targetHost = config.targetHost;
                this.serverCertificateThumbprint = config.serverCertificateThumbprint;
                this.serverCertificatePath = config.serverCertificatePath;
                this.clientCertificateThumbprint = config.clientCertificateThumbprint;
                this.clientCertificatePath = config.clientCertificatePath;
                this.clientCertificatePassword = config.clientCertificatePassword;
                this.sslProtocols = config.sslProtocols;
                this.pinCertificateAfterCA = config.pinCertificateAfterCA;
                this.pinCertificateBeforeCA = config.pinCertificateBeforeCA;
            }
            else
            {
                saveConfig("ZabbixConfig.json");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class DatabaseConfig
{
    private String url;
    private String user;
    private String password;
    private DatabaseType databaseType;
    private String sqlTasksPath;

    public DatabaseConfig()
    {
    }

    public DatabaseConfig(String url, String user, String password, DatabaseType databaseType, String sqlTasksPath)
    {
        this.url = url;
        this.user = user;
        this.password = password;
        this.databaseType = databaseType;
        this.sqlTasksPath = sqlTasksPath;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public DatabaseType getDatabaseType()
    {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType)
    {
        this.databaseType = databaseType;
    }

    public String getSqlTasksPath()
    {
        return sqlTasksPath;
    }

    public void setSqlTasksPath(String sqlTasksPath)
    {
        this.sqlTasksPath = sqlTasksPath;
    }

    // Save the configuration to a JSON file
    public void saveToFile(String filename) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(filename), this);
    }

    // Load the configuration from a JSON file
    public void loadFromFile(String filename) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            DatabaseConfig loadedConfig = mapper.readValue(new File(filename), DatabaseConfig.class);
            this.databaseType = loadedConfig.getDatabaseType();
            this.url = loadedConfig.getUrl();
            this.user = loadedConfig.getUser();
            this.password = loadedConfig.getPassword();
            this.sqlTasksPath = loadedConfig.getSqlTasksPath();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public String toString()
    {
        return "DatabaseConfig{" +
                "url='" + url + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", databaseType=" + databaseType +
                ", sqlTasksPath='" + sqlTasksPath + '\'' +
                '}';
    }
}

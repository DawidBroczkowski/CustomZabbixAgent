package org.example.agentTasks;

import org.example.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseMonitoringTask implements IZabbixTask
{

    private final DatabaseConfig config;

    public DatabaseMonitoringTask(@NamedParam("filePath") String filePath)
    {
        config = new DatabaseConfig();
        try
        {
            config.loadFromFile(filePath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<ZabbixTrapperItem> execute() throws Exception
    {
        ArrayList<ZabbixTrapperItem> items = new ArrayList<>();
        boolean isRunning = isDatabaseRunning();
        ZabbixTrapperItem item = new ZabbixTrapperItem(config.getDatabaseType().name() + ".status", isRunning ? "running" : "not running");
        items.add(item);
        return items;
    }

    @Override
    public ArrayList<CreateZabbixTrapperItem> getCreateZabbixTrapperItems()
    {
        return null;
    }

    private boolean isDatabaseRunning()
    {
        try (Connection connection = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword()))
        {
            if (connection != null)
            {
                try (Statement statement = connection.createStatement())
                {
                    try (ResultSet resultSet = statement.executeQuery("SELECT 1"))
                    {
                        if (resultSet.next())
                        {
                            return true;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }


}

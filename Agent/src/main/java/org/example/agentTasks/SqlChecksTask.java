package org.example.agentTasks;

import org.example.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class SqlChecksTask implements IZabbixTask
{
    private final DatabaseConfig config;
    TaskManager<SqlTaskEntry> taskManager;

    public SqlChecksTask(@NamedParam("filePath") String filePath)
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
        taskManager = new TaskManager<>(config.getSqlTasksPath());
        try
        {
            taskManager.loadTasksFromJson(SqlTaskEntry[].class);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<ZabbixTrapperItem> execute() throws Exception
    {
        ArrayList<ZabbixTrapperItem> items = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());)
        {
            for (SqlTaskEntry task : taskManager.getTasksSortedByPriority())
            {
                String result = executeSQL(connection, task.getSql());
                ZabbixTrapperItem item = new ZabbixTrapperItem(task.getKey(), result);
                items.add(item);
            }
        }

        return items;
    }

    private String executeSQL(Connection connection, String sql) throws Exception
    {
        try (Statement statement = connection.createStatement())
        {
            try (ResultSet resultSet = statement.executeQuery(sql))
            {
                if (resultSet.next())
                {
                    return resultSet.getString(1);
                }
            }
        }
        return "No result";
    }

    @Override
    public ArrayList<CreateZabbixTrapperItem> getCreateZabbixTrapperItems()
    {
        return null;
    }
}

package org.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class SqlTaskScheduler extends BaseScheduler<SqlTaskEntry>
{
    private final DatabaseConfig config;
    private final TaskManager<SqlTaskEntry> taskManager;

    public SqlTaskScheduler(ZabbixSender sender, String configFilePath)
    {
        super(sender);
        config = new DatabaseConfig();
        try
        {
            config.loadFromFile(configFilePath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        taskManager = new TaskManager<>(config.getSqlTasksPath());
        taskManager.loadTasksFromJson(SqlTaskEntry[].class);
        taskManager.getTasks().forEach(this::addTask);
    }

    @Override
    protected ArrayList<ZabbixTrapperItem> executeTask(SqlTaskEntry task) throws Exception
    {
        ArrayList<ZabbixTrapperItem> items = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword()))
        {
            String result = executeSQL(connection, task.getSql());
            ZabbixTrapperItem item = new ZabbixTrapperItem(task.getKey(), result);
            items.add(item);
        }
        return items;
    }

    private String executeSQL(Connection connection, String sql) throws Exception
    {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql))
        {
            if (resultSet.next())
            {
                return resultSet.getString(1);
            }
        }
        return "No result";
    }
}

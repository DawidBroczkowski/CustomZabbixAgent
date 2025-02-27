package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main
{
    public static void main(String[] args)
    {
        File logFile = new File("logging.properties");

        if (!logFile.exists()) {
            System.out.println("Logger config file not found");
            return;
        }
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(logFile);
        }
        catch (FileNotFoundException e)
        {

        }

        try
        {
            LogManager.getLogManager().readConfiguration(fis);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Logger logger = Logger.getLogger(Main.class.getName());
        ZabbixConfig zabbixConfig = new ZabbixConfig();
        zabbixConfig.loadConfig("ZabbixConfig.json");
        ZabbixClient client = new ZabbixClient(zabbixConfig);
        logger.info("TLS protocol: " + client.getConfig().getSslProtocols());

        // Load tasks from JSON
        List<TaskConfigEntry> taskConfigs = TaskLoader.loadTasks("tasks.json");

        if (taskConfigs != null)
        {
            for (TaskConfigEntry taskConfig : taskConfigs)
            {
                try
                {
                    // Load task instance dynamically with parameters
                    IZabbixTask taskInstance = TaskFactory.createTask(taskConfig.getTaskClass(), taskConfig.getParams());
                    TimeUnit timeUnit = TimeUnit.valueOf(taskConfig.getTimeUnit());
                    client.getScheduler().addTask(taskInstance, taskConfig.getInterval(), timeUnit);
                    logger.fine("Added task " + taskConfig.getTaskClass());
                }
                catch (Exception e)
                {
                    logger.severe("Exception thrown in Main: " + e.getMessage());
                    logger.fine(e.getStackTrace().toString());
                }
            }
        }
        client.getScheduler().start();
        ScriptTaskScheduler scriptTaskScheduler = new ScriptTaskScheduler(client.getSender(), "scriptTasks.json", "scriptConfig.json");
        scriptTaskScheduler.start();
        SqlTaskScheduler sqlTaskScheduler = new SqlTaskScheduler(client.getSender(), "databaseMonitoringConfig.json");
        sqlTaskScheduler.start();

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            logger.info("Shutting down...");
            if (client != null)
            {
                client.getScheduler().stop(); // Assuming stop() method gracefully shuts down the scheduler
            }
            if (scriptTaskScheduler != null)
            {
                scriptTaskScheduler.stop(); // Ensure ScriptTaskScheduler has a stop() method
            }
            if (sqlTaskScheduler != null)
            {
                sqlTaskScheduler.stop(); // Ensure SqlTaskScheduler has a stop() method
            }
            logger.info("Shutdown completed.");
        }));

        //System.out.println("Press any key to exit.");
        //try
        //{
        //    System.in.read();
        //}
        //catch (IOException e)
        //{
        //    e.printStackTrace();
        //}
    }
}

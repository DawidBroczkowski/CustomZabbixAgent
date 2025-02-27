package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ScriptTaskScheduler extends BaseScheduler<ScriptTaskEntry>
{
    private final TaskManager<ScriptTaskEntry> taskManager;
    private ScriptConfig scriptConfig;
    private static final Logger logger = Logger.getLogger(ScriptTaskScheduler.class.getName());

    public ScriptTaskScheduler(ZabbixSender sender, String tasksFilePath, String configFilePath)
    {
        super(sender);
        taskManager = new TaskManager(tasksFilePath);
        taskManager.loadTasksFromJson(ScriptTaskEntry[].class);
        taskManager.getTasks().forEach(this::addTask);
        scriptConfig = new ScriptConfig();
        try
        {
            scriptConfig.loadFromFile(configFilePath);
        }
        catch (IOException e)
        {
            logger.severe("Exception thrown in ScriptTaskScheduler: " + e.getMessage());
            logger.fine(e.getStackTrace().toString());
        }
    }

    @Override
    protected ArrayList<ZabbixTrapperItem> executeTask(ScriptTaskEntry task) throws Exception
    {
        ArrayList<ZabbixTrapperItem> items = new ArrayList<>();
        String[] command = scriptConfig.getCommandArray(task.getScriptPath());
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
        {
            output.append(line).append(System.lineSeparator());
        }

        process.waitFor();
        ZabbixTrapperItem item = new ZabbixTrapperItem(task.getKey(), output.toString().trim());
        items.add(item);

        return items;
    }
}

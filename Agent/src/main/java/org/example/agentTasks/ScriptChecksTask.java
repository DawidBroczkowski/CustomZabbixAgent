package org.example.agentTasks;

import org.example.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ScriptChecksTask implements IZabbixTask
{
    private String[] command;
    private TaskManager<ScriptTaskEntry> taskManager;
    ScriptConfig config;

    public ScriptChecksTask(@NamedParam("filePath") String filePath, @NamedParam("configPath") String configPath)
    {
        taskManager = new TaskManager(filePath);
        try
        {
            taskManager.loadTasksFromJson(ScriptTaskEntry[].class);
            config = new ScriptConfig();
            config.loadFromFile(configPath);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ArrayList<ZabbixTrapperItem> execute() throws Exception
    {
        ArrayList<ZabbixTrapperItem> items = new ArrayList<>();

        for (ScriptTaskEntry task : taskManager.getTasksSortedByPriority())
        {
            command = config.getCommandArray(task.getScriptPath());
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            // Start the process
            Process process = null;
            try
            {
                process = processBuilder.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            // Capture the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while (true)
            {
                try
                {
                    if (!((line = reader.readLine()) != null))
                    {
                        break;
                    }
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                output.append(line).append(System.lineSeparator());
            }

            // Wait for the process to finish and get the exit code
            int exitCode = 0;
            try
            {
                exitCode = process.waitFor();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            ZabbixTrapperItem item = new ZabbixTrapperItem(task.getKey(), output.toString().trim());
            items.add(item);
        }
        return items;
    }

    @Override
    public ArrayList<CreateZabbixTrapperItem> getCreateZabbixTrapperItems()
    {
        return null;
    }
}

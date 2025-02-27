package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProcessManager
{
    private final String filePath;
    private List<String> processNames;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(ProcessManager.class.getName());

    public ProcessManager(String filePath)
    {
        this.filePath = filePath;
    }

    public void loadConfigFromJson()
    {
        File file = new File(filePath);
        if (file.length() == 0 || !file.exists())
        {
            logger.warning("Process config file is empty or doesn't exist");
            processNames = new ArrayList<>();
            return;
        }
        try
        {
            processNames = objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        }
        catch (Exception e)
        {
            logger.severe("Exception thrown in ProcessManager: " + e.getMessage());
            logger.fine(e.getStackTrace().toString());
        }
    }

    public void saveConfig()
    {
        try
        {
            objectMapper.writeValue(new File(filePath), processNames);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public List<String> getProcessNames()
    {
        return processNames;
    }

    public void addProcessName(String processName)
    {
        if (!processNames.contains(processName))
        {
            processNames.add(processName);
            saveConfig();
        }
    }

    public void removeProcessName(String processName)
    {
        if (processNames.remove(processName))
        {
            saveConfig();
        }
    }
}
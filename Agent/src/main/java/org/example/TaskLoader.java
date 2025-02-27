package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class TaskLoader
{
    private static final Logger logger = Logger.getLogger(TaskLoader.class.getName());
    public static List<TaskConfigEntry> loadTasks(String filename)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            return mapper.readValue(new File(filename), new TypeReference<List<TaskConfigEntry>>()
            {
            });
        }
        catch (IOException e)
        {
            logger.severe("Exception thrown in TaskLoader: " + e.getMessage());
            logger.fine(e.getStackTrace().toString());
            return null;
        }
    }
}

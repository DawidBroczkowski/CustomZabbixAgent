package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScriptConfig
{

    private List<String> command;

    // Default constructor needed for Jackson deserialization
    public ScriptConfig()
    {
    }

    public ScriptConfig(List<String> command)
    {
        this.command = command;
    }

    public List<String> getCommand()
    {
        return command;
    }

    public void setCommand(List<String> command)
    {
        this.command = command;
    }

    public String[] getCommandArray(String scriptPath)
    {
        List<String> fullCommand = new ArrayList<>(command);
        fullCommand.add(scriptPath);
        return fullCommand.toArray(new String[0]);
    }

    // Serialize the ScriptConfig to a JSON file
    public void saveToFile(String filePath) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(filePath), this);
    }

    // Deserialize a ScriptConfig from a JSON file
    public void loadFromFile(String filePath) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        List<String> command = mapper.readValue(new File(filePath), List.class);
        this.command = command;
    }

}

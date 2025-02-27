package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class COMPortsManager
{

    private List<String> comPortNames;

    public COMPortsManager()
    {
        comPortNames = new ArrayList<>();
    }

    public void loadConfigFromFile(String filePath)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            // Read the JSON file into a JsonNode
            JsonNode rootNode = mapper.readTree(new File(filePath));

            // Get the "com_port_info" node, if it exists
            JsonNode comPortInfoNode = rootNode.path("com_port_info");

            // Check if the node is an array
            if (comPortInfoNode.isArray())
            {
                for (JsonNode node : comPortInfoNode)
                {
                    // Extract the port name
                    JsonNode portNameNode = node.path("portName");
                    if (portNameNode.isTextual())
                    {
                        comPortNames.add(portNameNode.asText());
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public List<String> getComPortNames()
    {
        return comPortNames;
    }
}

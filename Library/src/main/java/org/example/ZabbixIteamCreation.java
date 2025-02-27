package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;

public class ZabbixIteamCreation
{
    private ArrayList<CreateZabbixTrapperItem> items = new ArrayList<CreateZabbixTrapperItem>();

    public void addTrapperItem(CreateZabbixTrapperItem item)
    {
        items.add(item);
    }

    public void addTrapperItems(ArrayList<CreateZabbixTrapperItem> items)
    {
        this.items.addAll(items);
    }

    public void saveTrapperItemsToFile(String filePath)
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            mapper.writeValue(new File(filePath), items);
            System.out.println("Items saved to file: " + filePath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Failed to save items to file: " + filePath);
        }
    }
}

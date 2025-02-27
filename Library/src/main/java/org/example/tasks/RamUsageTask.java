package org.example.tasks;

import org.example.CreateZabbixTrapperItem;
import org.example.IZabbixTask;
import org.example.ZabbixClient;
import org.example.ZabbixTrapperItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RamUsageTask implements IZabbixTask {
    Runtime runtime = Runtime.getRuntime();
    @Override
    public ArrayList<ZabbixTrapperItem> execute() {
        ZabbixTrapperItem item = new ZabbixTrapperItem("ram.usage",
                String.valueOf((runtime.totalMemory() - runtime.freeMemory())));

        ArrayList<ZabbixTrapperItem> items = new ArrayList<>();
        items.add(item);
        return items;
    }

    @Override
    public ArrayList<CreateZabbixTrapperItem> getCreateZabbixTrapperItems()
    {
        ArrayList<CreateZabbixTrapperItem> items = new ArrayList<>();
        items.add(new CreateZabbixTrapperItem("RAM usage", "ram.usage", 3, "0", "%"));
        return items;
    }
}
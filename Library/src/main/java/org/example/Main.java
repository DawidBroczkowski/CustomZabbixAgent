package org.example;

import org.example.tasks.CpuUsageTask;
import org.example.tasks.RamUsageTask;
import org.example.tasks.ThreadUsageTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main
{
    public static void main(String[] args)
    {
        ZabbixConfig zabbixConfig = new ZabbixConfig();
        zabbixConfig.loadConfig("ZabbixConfig.json");
        ZabbixClient client = new ZabbixClient(zabbixConfig);
        System.out.println(client.getConfig().getSslProtocols());
        CpuUsageTask cpuUsageTask = new CpuUsageTask();
        ThreadUsageTask threadUsageTask = new ThreadUsageTask();
        RamUsageTask ramUsageTask = new RamUsageTask();

        ArrayList<IZabbixTask> tasks = new ArrayList<>();
        tasks.add(cpuUsageTask);
        tasks.add(threadUsageTask);
        tasks.add(ramUsageTask);
        ZabbixIteamCreation zabbixIteamCreation = new ZabbixIteamCreation();

        for (IZabbixTask task : tasks)
        {
            zabbixIteamCreation.addTrapperItems(task.getCreateZabbixTrapperItems());
        }

        zabbixIteamCreation.saveTrapperItemsToFile("itemsCreation.json");

        client.getScheduler().addTask(cpuUsageTask, 2500, TimeUnit.MILLISECONDS);
        client.getScheduler().addTask(threadUsageTask, 1, TimeUnit.SECONDS);
        client.getScheduler().addTask(ramUsageTask, 1, TimeUnit.SECONDS);
        client.getScheduler().start();

        System.out.println("Press any key to exit.");
        try
        {
            System.in.read();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
package org.example.agentTasks;

import org.example.CreateZabbixTrapperItem;
import org.example.IZabbixTask;
import org.example.ZabbixTrapperItem;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.util.ArrayList;

public class SystemCpuUsageTask implements IZabbixTask
{
    SystemInfo systemInfo = new SystemInfo();
    CentralProcessor processor = systemInfo.getHardware().getProcessor();

    @Override
    public ArrayList<ZabbixTrapperItem> execute() throws Exception
    {
        double cpuLoad = processor.getSystemCpuLoad(1000) * 100;
        ArrayList<ZabbixTrapperItem> items = new ArrayList<>();
        ZabbixTrapperItem item = new ZabbixTrapperItem("system.cpu.usage", String.valueOf(cpuLoad));
        items.add(item);
        return items;
    }

    @Override
    public ArrayList<CreateZabbixTrapperItem> getCreateZabbixTrapperItems()
    {
        return null;
    }
}

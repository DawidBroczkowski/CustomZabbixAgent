package org.example.agentTasks;

import org.example.CreateZabbixTrapperItem;
import org.example.IZabbixTask;
import org.example.ZabbixTrapperItem;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

import java.util.ArrayList;
import java.util.Arrays;

public class SystemRamUsageTask implements IZabbixTask
{
    SystemInfo systemInfo = new SystemInfo();
    GlobalMemory memory = systemInfo.getHardware().getMemory();

    @Override
    public ArrayList<ZabbixTrapperItem> execute() throws Exception
    {
        long totalMemory = memory.getTotal();
        long freeMemory = memory.getAvailable();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (((double) usedMemory / (double) totalMemory) * 100);

        return new ArrayList<ZabbixTrapperItem>(Arrays.asList(
                new ZabbixTrapperItem("system.ram.total", String.valueOf((double) totalMemory / 1024 / 1024)),
                new ZabbixTrapperItem("system.ram.free", String.valueOf((double) freeMemory / 1024 / 1024)),
                new ZabbixTrapperItem("system.ram.used", String.valueOf((double) usedMemory / 1024 / 1024)),
                new ZabbixTrapperItem("system.ram.usage", String.valueOf(memoryUsage))
        ));
    }

    @Override
    public ArrayList<CreateZabbixTrapperItem> getCreateZabbixTrapperItems()
    {
        return null;
    }
}

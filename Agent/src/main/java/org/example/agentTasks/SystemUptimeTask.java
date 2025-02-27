package org.example.agentTasks;

import org.example.CreateZabbixTrapperItem;
import org.example.IZabbixTask;
import org.example.ZabbixTrapperItem;
import oshi.SystemInfo;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SystemUptimeTask implements IZabbixTask
{
    SystemInfo systemInfo = new SystemInfo();

    @Override
    public ArrayList<ZabbixTrapperItem> execute() throws Exception
    {
        ArrayList<ZabbixTrapperItem> items = new ArrayList<>();

        long uptime = systemInfo.getOperatingSystem().getSystemUptime();

        ZabbixTrapperItem item = new ZabbixTrapperItem("system.uptime", formatUptime(uptime));
        items.add(item);

        return items;
    }

    private String formatUptime(long seconds)
    {
        long days = TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60;
        long secs = seconds % 60;

        return String.format("%d days, %02d:%02d:%02d", days, hours, minutes, secs);
    }

    @Override
    public ArrayList<CreateZabbixTrapperItem> getCreateZabbixTrapperItems()
    {
        return null;
    }
}

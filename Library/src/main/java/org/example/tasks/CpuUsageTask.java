package org.example.tasks;

import org.example.*;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import com.sun.management.OperatingSystemMXBean;

public class CpuUsageTask implements IZabbixTask {
    OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    double value = 0;
    @Override
    public ArrayList<ZabbixTrapperItem> execute() {
        try {
            // Initial call to getProcessCpuLoad() to start measurement
            value = osBean.getProcessCpuLoad();
            // Sleep to allow some time for an accurate measurement
            Thread.sleep(1000);
            // Second call to get the actual CPU load
            value = osBean.getProcessCpuLoad();
            ZabbixTrapperItem item = new ZabbixTrapperItem("cpu.usage", String.format(Locale.US, "%.2f", value * 100));
            ArrayList<ZabbixTrapperItem> items = new ArrayList<>();
            items.add(item);
            return items;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during CPU usage measurement", e);
        }
    }

    @Override
    public ArrayList<CreateZabbixTrapperItem> getCreateZabbixTrapperItems()
    {
        ArrayList<CreateZabbixTrapperItem> items = new ArrayList<>();
        items.add(new CreateZabbixTrapperItem("CPU usage", "cpu.usage", 3, "0", "%"));
        return items;
    }
}

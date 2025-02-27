package org.example.tasks;

import org.example.CreateZabbixTrapperItem;
import org.example.IZabbixTask;
import org.example.ZabbixTrapperItem;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ThreadUsageTask implements IZabbixTask {
    ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    @Override
    public ArrayList<ZabbixTrapperItem> execute() {
        ZabbixTrapperItem item = new ZabbixTrapperItem("thread.count", String.valueOf(threadMXBean.getThreadCount()));
        ArrayList<ZabbixTrapperItem> items = new ArrayList<ZabbixTrapperItem>();
        items.add(item);
        return items;
    }

    @Override
    public ArrayList<CreateZabbixTrapperItem> getCreateZabbixTrapperItems()
    {
        ArrayList<CreateZabbixTrapperItem> items = new ArrayList<>();
        items.add(new CreateZabbixTrapperItem("Thread count", "thread.count", 3, "0", ""));
        return items;
    }
}
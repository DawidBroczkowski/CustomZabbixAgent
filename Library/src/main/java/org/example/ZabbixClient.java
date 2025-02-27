package org.example;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ZabbixClient
{
    private final ZabbixConfig config;
    private final ZabbixSender sender;
    private final ZabbixScheduler scheduler;

    public ZabbixClient()
    {
        this.config = new ZabbixConfig();
        this.sender = new ZabbixSender(config);
        this.scheduler = new ZabbixScheduler(sender);
    }

    public ZabbixClient(ZabbixConfig config)
    {
        this.config = config;
        this.sender = new ZabbixSender(this.config);
        this.scheduler = new ZabbixScheduler(sender);
    }

    public ZabbixConfig getConfig()
    {
        return config;
    }

    public ZabbixSender getSender()
    {
        return sender;
    }

    public ZabbixScheduler getScheduler()
    {
        return scheduler;
    }
}

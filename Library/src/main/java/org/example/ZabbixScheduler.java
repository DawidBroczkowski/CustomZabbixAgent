package org.example;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ZabbixScheduler
{

    private final ZabbixSender sender;
    private final Map<IZabbixTask, ScheduledFuture<?>> taskFutures = new ConcurrentHashMap<>();
    private final List<TaskConfig> taskConfigs = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService scheduler;
    private ExecutorService asyncExecutor;
    private static final Logger logger = Logger.getLogger(ZabbixScheduler.class.getName());

    public ZabbixScheduler(ZabbixSender sender)
    {
        this.sender = sender;
    }

    public void addTask(IZabbixTask task, long interval, TimeUnit unit)
    {
        taskConfigs.add(new TaskConfig(task, interval, unit));
        if (scheduler != null && !scheduler.isShutdown())
        {
            startTask(task, interval, unit);
        }
    }

    public void start()
    {
        if (scheduler != null && !scheduler.isShutdown())
        {
            throw new IllegalStateException("Scheduler is already running");
        }

        scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory()
        {
            @Override
            public Thread newThread(Runnable r)
            {
                Thread t = new Thread(r);
                t.setName("ZabbixSchedulerThread");
                t.setDaemon(true);
                return t;
            }
        });

        asyncExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (TaskConfig config : taskConfigs)
        {
            startTask(config.task, config.interval, config.unit);
        }
    }

    public void stop()
    {
        if (scheduler == null || scheduler.isShutdown())
        {
            throw new IllegalStateException("Scheduler is not running");
        }

        for (ScheduledFuture<?> future : taskFutures.values())
        {
            future.cancel(true);
        }
        taskFutures.clear();

        asyncExecutor.shutdown(); // Shut down the asyncExecutor
        scheduler.shutdown(); // Shut down the scheduler

        try
        {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS))
            {
                scheduler.shutdownNow();
                if (!asyncExecutor.awaitTermination(60, TimeUnit.SECONDS))
                {
                    asyncExecutor.shutdownNow(); // Ensure asyncExecutor is also shut down
                }
            }
        }
        catch (InterruptedException e)
        {
            scheduler.shutdownNow();
            asyncExecutor.shutdownNow(); // Ensure asyncExecutor is also shut down
            Thread.currentThread().interrupt(); // Preserve interrupt status
        }
    }

    public void shutdown()
    {
        stop();
        taskConfigs.clear(); // Clear the stored task configurations
    }

    private void startTask(IZabbixTask task, long interval, TimeUnit unit)
    {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() ->
        {
            CompletableFuture.runAsync(() ->
            {
                try
                {
                    ArrayList<ZabbixTrapperItem> items = task.execute();
                    if (items != null && !items.isEmpty())
                    {
                        sender.sendItemsAsync(items)
                                .thenAccept(response ->
                                {
                                    for (ZabbixTrapperItem item : items)
                                    {

                                        logger.fine("Sent [Key: " + item.getKey() + " | Value: " + item.getValue() + "]");
                                    }
                                })
                                .exceptionally(e ->
                                {
                                    logger.severe("Exception thrown in ZabbixSender: " + e.getMessage());
                                    logger.fine(e.getStackTrace().toString());
                                    for (ZabbixTrapperItem item : items)
                                    {

                                        logger.fine("Error sending item [" + item.getKey() + ", " + item.getValue() + "]: ");
                                    }
                                    return null;
                                });
                    }
                }
                catch (Exception e)
                {
                    logger.severe("Exception thrown in ZabbixScheduler: " + e.getMessage());
                    logger.fine(e.getStackTrace().toString());
                }
            }, asyncExecutor);
        }, 0, interval, unit);
        taskFutures.put(task, future);
    }

    private static class TaskConfig
    {
        final IZabbixTask task;
        final long interval;
        final TimeUnit unit;

        TaskConfig(IZabbixTask task, long interval, TimeUnit unit)
        {
            this.task = task;
            this.interval = interval;
            this.unit = unit;
        }
    }
}

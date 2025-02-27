package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public abstract class BaseScheduler<T extends ITaskEntry>
{

    protected final ZabbixSender sender;
    protected final Map<T, ScheduledFuture<?>> taskFutures = new ConcurrentHashMap<>();
    protected final List<TaskConfig> taskConfigs = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService scheduler;
    private ExecutorService asyncExecutor;
    private static final Logger logger = Logger.getLogger(BaseScheduler.class.getName());

    public BaseScheduler(ZabbixSender sender)
    {
        this.sender = sender;
    }

    public void addTask(T task)
    {
        taskConfigs.add(new TaskConfig(task, task.getInterval(), task.getTimeUnit()));
        if (scheduler != null && !scheduler.isShutdown())
        {
            startTask(task, task.getInterval(), task.getTimeUnit());
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
                t.setName("BaseSchedulerThread");
                t.setDaemon(true);
                return t;
            }
        });

        asyncExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (TaskConfig config : taskConfigs)
        {
            startTask((T) config.task, config.interval, config.unit);
        }
    }

    private void startTask(T task, long interval, TimeUnit unit)
    {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> CompletableFuture.runAsync(() ->
        {
            try
            {
                ArrayList<ZabbixTrapperItem> items = executeTask(task);
                if (items != null && !items.isEmpty())
                {
                    sender.sendItemsAsync(items)
                            .thenAccept(response ->
                            {
                                taskFutures.remove(task);
                                for (ZabbixTrapperItem item : items)
                                {
                                    logger.finer("Sent [Key: " + item.getKey() + " | Value: " + item.getValue() + "]");
                                }
                            })
                            .exceptionally(e ->
                            {
                                for (ZabbixTrapperItem item : items)
                                {
                                    logger.finer("Error sending item (" + item.getKey() + ", " + item.getValue() + "): " + e.getMessage());
                                }
                                taskFutures.remove(task);
                                return null;
                            });
                }
            }
            catch (Exception e)
            {
                logger.severe("Exception thrown in BaseScheduler: " + e.getMessage());
                logger.fine(e.getStackTrace().toString());
            }
        }, asyncExecutor), 0, interval, unit);
        taskFutures.put(task, future);
    }

    protected abstract ArrayList<ZabbixTrapperItem> executeTask(T task) throws Exception;

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

    private static class TaskConfig
    {
        final ITaskEntry task;
        final long interval;
        final TimeUnit unit;

        TaskConfig(ITaskEntry task, long interval, TimeUnit unit)
        {
            this.task = task;
            this.interval = interval;
            this.unit = unit;
        }
    }
}

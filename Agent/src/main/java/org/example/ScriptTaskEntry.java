package org.example;

import java.util.concurrent.TimeUnit;

public class ScriptTaskEntry implements ITaskEntry
{
    private String key;
    private String scriptPath;
    private int priority;
    private int type;
    private String unit;
    private int interval;
    private TimeUnit timeUnit;

    public ScriptTaskEntry(String key, String scriptPath, int priority, int type, String unit)
    {
        this.key = key;
        this.scriptPath = scriptPath;
        this.priority = priority;
        this.type = type;
        this.unit = unit;
    }

    public ScriptTaskEntry()
    {
    }

    @Override
    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getScriptPath()
    {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath)
    {
        this.scriptPath = scriptPath;
    }

    @Override
    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    @Override
    public int getInterval()
    {
        return interval;
    }

    public void setInterval(int interval)
    {
        this.interval = interval;
    }

    @Override
    public TimeUnit getTimeUnit()
    {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit)
    {
        this.timeUnit = timeUnit;
    }
}

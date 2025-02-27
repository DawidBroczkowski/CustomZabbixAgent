package org.example;

import java.util.concurrent.TimeUnit;

public class SqlTaskEntry implements ITaskEntry
{
    private String key;
    private String sql;
    private int priority;
    private int type;
    private String unit;
    private int interval;
    private TimeUnit timeUnit;

    public SqlTaskEntry(String key, String sql, int priority, int type, String unit)
    {
        this.key = key;
        this.sql = sql;
        this.priority = priority;
        this.type = type;
        this.unit = unit;
    }

    public SqlTaskEntry()
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

    public String getSql()
    {
        return sql;
    }

    public void setSql(String sql)
    {
        this.sql = sql;
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

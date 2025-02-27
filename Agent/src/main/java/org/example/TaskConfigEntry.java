package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class TaskConfigEntry
{
    @JsonProperty("taskClass")
    private String taskClass;

    @JsonProperty("interval")
    private int interval;

    @JsonProperty("timeUnit")
    private String timeUnit;

    @JsonProperty("params")
    private Map<String, Object> params;

    // Getters and Setters
    public String getTaskClass()
    {
        return taskClass;
    }

    public void setTaskClass(String taskClass)
    {
        this.taskClass = taskClass;
    }

    public int getInterval()
    {
        return interval;
    }

    public void setInterval(int interval)
    {
        this.interval = interval;
    }

    public String getTimeUnit()
    {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit)
    {
        this.timeUnit = timeUnit;
    }

    public Map<String, Object> getParams()
    {
        return params;
    }

    public void setParams(Map<String, Object> params)
    {
        this.params = params;
    }
}

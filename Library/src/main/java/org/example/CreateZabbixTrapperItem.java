package org.example;

public class CreateZabbixTrapperItem
{
    private String name;
    private String key;
    private int value_type;
    private String delay;
    private String units;

    public CreateZabbixTrapperItem(String name, String key, int value_type, String delay, String units)
    {
        this.name = name;
        this.key = key;
        this.value_type = value_type;
        this.delay = delay;
        this.units = units;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public int getValue_type()
    {
        return value_type;
    }

    public void setValue_type(int value_type)
    {
        this.value_type = value_type;
    }

    public String getDelay()
    {
        return delay;
    }

    public void setDelay(String delay)
    {
        this.delay = delay;
    }

    public String getUnits()
    {
        return units;
    }

    public void setUnits(String units)
    {
        this.units = units;
    }
}

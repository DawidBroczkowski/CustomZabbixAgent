package org.example;

import java.util.concurrent.TimeUnit;

public interface ITaskEntry
{
    String getKey();

    int getPriority();

    int getInterval();

    TimeUnit getTimeUnit();
}


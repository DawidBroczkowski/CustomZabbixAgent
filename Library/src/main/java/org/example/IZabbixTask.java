package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IZabbixTask
{
    ArrayList<ZabbixTrapperItem> execute() throws Exception;

    ArrayList<CreateZabbixTrapperItem> getCreateZabbixTrapperItems();
}
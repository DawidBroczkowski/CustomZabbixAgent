package org.example.agentTasks;

import org.example.CreateZabbixTrapperItem;
import org.example.IZabbixTask;
import org.example.ZabbixTrapperItem;
import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

import java.util.ArrayList;


public class SystemDiskUsageTask implements IZabbixTask
{
    SystemInfo systemInfo = new SystemInfo();
    FileSystem fileSystem = systemInfo.getOperatingSystem().getFileSystem();

    @Override
    public ArrayList<ZabbixTrapperItem> execute() throws Exception
    {
        ArrayList<ZabbixTrapperItem> items = new ArrayList<>();

        for (OSFileStore fs : fileSystem.getFileStores())
        {
            long totalSpace = fs.getTotalSpace();
            long usableSpace = fs.getUsableSpace();
            long usedSpace = totalSpace - usableSpace;

            // Create a ZabbixTrapperItem for each filesystem
            ZabbixTrapperItem item = new ZabbixTrapperItem("vfs.fs.size[" + fs.getMount() + ",total]", String.valueOf((double) totalSpace / 1024 / 1024));
            items.add(item);

            item = new ZabbixTrapperItem("vfs.fs.size[" + fs.getMount() + ",used]", String.valueOf((double) usedSpace / 1024 / 1024));
            items.add(item);

            item = new ZabbixTrapperItem("vfs.fs.size[" + fs.getMount() + ",free]", String.valueOf((double) usableSpace / 1024 / 1024));
            items.add(item);
        }

        return items;
    }

    @Override
    public ArrayList<CreateZabbixTrapperItem> getCreateZabbixTrapperItems()
    {
        return null;
    }
}

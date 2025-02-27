package org.example.agentTasks;

import org.example.*;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.Locale;

public class SystemProcessMonitoringTask implements IZabbixTask
{

    SystemInfo systemInfo = new SystemInfo();
    OperatingSystem os = systemInfo.getOperatingSystem();
    ProcessManager configManager;

    public SystemProcessMonitoringTask(@NamedParam("filePath") String filePath)
    {
        configManager = new ProcessManager(filePath);
        configManager.loadConfigFromJson();
    }

    @Override
    public ArrayList<ZabbixTrapperItem> execute() throws Exception
    {
        configManager.saveConfig();
        ArrayList<ZabbixTrapperItem> items = new ArrayList<>();
        ArrayList<String> processNames = (ArrayList) configManager.getProcessNames();

        for (String processName : processNames)
        {
            OSProcess process = getProcessByName(processName);
            if (process != null)
            {
                int pid = process.getProcessID();
                double cpuLoad = calculateCpuPercent(process);
                long memory = process.getResidentSetSize();

                // Create ZabbixTrapperItems for each key process
                ZabbixTrapperItem item = new ZabbixTrapperItem("process." + processName + ".cpu", String.format(Locale.US, "%.2f", cpuLoad));
                items.add(item);

                item = new ZabbixTrapperItem("process." + processName + ".memory", String.valueOf((double) memory / 1024 / 1024));
                items.add(item);

                item = new ZabbixTrapperItem("process." + processName + ".status", getProcessStatus(process));
                items.add(item);
            }
            else
            {
                ZabbixTrapperItem item = new ZabbixTrapperItem("process." + processName + ".status", "not found");
                items.add(item);
            }
        }

        return items;
    }

    private OSProcess getProcessByName(String name)
    {
        ArrayList<OSProcess> processes = (ArrayList) os.getProcesses();
        for (OSProcess process : processes)
        {
            if (process.getName().contains(name))
            {
                return process;
            }
        }
        return null;
    }

    private double calculateCpuPercent(OSProcess process)
    {
        long upTime = process.getUpTime(); // milliseconds
        long kernelTime = process.getKernelTime(); // milliseconds
        long userTime = process.getUserTime(); // milliseconds

        if (upTime > 0)
        {
            return 100d * (kernelTime + userTime) / upTime;
        }
        else
        {
            return 0d;
        }
    }

    private String getProcessStatus(OSProcess process)
    {
        switch (process.getState())
        {
            case RUNNING:
                return "running";
            case SLEEPING:
                return "sleeping";
            case WAITING:
                return "waiting";
            case ZOMBIE:
                return "zombie";
            case STOPPED:
                return "stopped";
            case OTHER:
                return "other";
            default:
                return "unknown";
        }
    }

    @Override
    public ArrayList<CreateZabbixTrapperItem> getCreateZabbixTrapperItems()
    {
        return null;
    }
}

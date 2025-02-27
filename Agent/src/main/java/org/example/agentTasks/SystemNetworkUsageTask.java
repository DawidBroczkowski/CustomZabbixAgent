package org.example.agentTasks;

import org.example.CreateZabbixTrapperItem;
import org.example.IZabbixTask;
import org.example.ZabbixTrapperItem;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import java.util.ArrayList;
import java.util.List;

public class SystemNetworkUsageTask implements IZabbixTask
{
    SystemInfo systemInfo = new SystemInfo();

    @Override
    public ArrayList<ZabbixTrapperItem> execute() throws Exception
    {
        ArrayList<ZabbixTrapperItem> items = new ArrayList<>();

        List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();
        for (NetworkIF netIF : networkIFs)
        {
            netIF.updateAttributes();
            long bytesReceived = netIF.getBytesRecv();
            long bytesSent = netIF.getBytesSent();

            // Get local IP addresses
            List<String> localIPs = new ArrayList<>();
            for (String ip : netIF.getIPv4addr())
            {
                localIPs.add(ip);
            }
            for (String ip : netIF.getIPv6addr())
            {
                localIPs.add(ip);
            }

            // Create item for received bytes
            ZabbixTrapperItem item = new ZabbixTrapperItem("net.if.in[" + netIF.getName() + "]", String.valueOf((double) bytesReceived / 1024 / 1024));
            items.add(item);

            // Create item for sent bytes
            item = new ZabbixTrapperItem("net.if.out[" + netIF.getName() + "]", String.valueOf((double) bytesSent / 1024 / 1024));
            items.add(item);

            // Create item for local IP addresses
            String localIpString = String.join(", ", localIPs);
            item = new ZabbixTrapperItem("net.if.localIp[" + netIF.getName() + "]", localIpString);
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

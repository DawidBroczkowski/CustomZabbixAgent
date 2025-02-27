package org.example.agentTasks;

import com.fazecast.jSerialComm.SerialPort;
import org.example.*;

import java.util.ArrayList;
import java.util.List;

public class SystemCOMPortTask implements IZabbixTask
{

    private COMPortsManager configManager;

    public SystemCOMPortTask(@NamedParam("filePath") String filePath)
    {
        configManager = new COMPortsManager();
        configManager.loadConfigFromFile(filePath);
    }

    @Override
    public ArrayList<ZabbixTrapperItem> execute() throws Exception
    {
        ArrayList<ZabbixTrapperItem> items = new ArrayList<>();
        List<String> portNames = configManager.getComPortNames();

        // Get all available serial ports
        SerialPort[] availablePorts = SerialPort.getCommPorts();

        // Create a list of available port names for quick lookup
        List<String> availablePortNames = new ArrayList<>();
        for (SerialPort port : availablePorts)
        {
            availablePortNames.add(port.getSystemPortName());
        }

        // Check each port name from the config and report its availability
        for (String portName : portNames)
        {
            boolean isAvailable = availablePortNames.contains(portName);
            // Create a ZabbixTrapperItem for each COM port availability
            items.add(new ZabbixTrapperItem("com.port.available[" + portName + "]", isAvailable ? "1" : "0"));
        }

        return items;
    }

    @Override
    public ArrayList<CreateZabbixTrapperItem> getCreateZabbixTrapperItems()
    {
        return null;
    }
}

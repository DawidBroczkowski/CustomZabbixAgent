package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaskManager<T extends ITaskEntry>
{
    private List<T> tasks;
    private String filePath;
    private ObjectMapper mapper = new ObjectMapper();

    public TaskManager(String filePath)
    {
        this.tasks = new ArrayList<>();
        this.filePath = filePath;

        // create file if it doesn't exist
        try
        {
            File file = new File(filePath);
            if (file.createNewFile())
            {
                System.out.println("File created: " + file.getName());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void addTask(T task)
    {
        tasks.add(task);
    }

    public ArrayList<T> getTasksSortedByPriority()
    {
        ArrayList<T> sortedTasks = new ArrayList<>(tasks);
        sortedTasks.sort(Comparator.comparingInt(ITaskEntry::getPriority));
        return sortedTasks;
    }

    public void loadTasksFromJson(Class<T[]> clazz)
    {
        File file = new File(filePath);
        if (file.length() == 0 || !file.exists())
        {
            System.out.println("Tasks file is empty or doesn't exist");
            tasks = new ArrayList<>();
            return;
        }
        try
        {
            T[] tasksArray = mapper.readValue(file, clazz);
            for (T task : tasksArray)
            {
                addTask(task);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public List<T> getTasks()
    {
        return tasks;
    }
}

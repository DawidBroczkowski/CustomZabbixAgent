package org.example;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class TaskFactory
{
    public static IZabbixTask createTask(String taskClassName, Map<String, Object> params)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
    {
        Class<?> clazz = Class.forName(taskClassName);

        // Find appropriate constructor based on params
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors)
        {
            Parameter[] parameters = constructor.getParameters();
            if (parameters.length == params.size())
            {
                Object[] paramValues = new Object[parameters.length];
                boolean match = true;
                for (int i = 0; i < parameters.length; i++)
                {
                    NamedParam namedParam = parameters[i].getAnnotation(NamedParam.class);
                    if (namedParam != null && params.containsKey(namedParam.value()))
                    {
                        paramValues[i] = convertToType(parameters[i].getType(), params.get(namedParam.value()));
                    }
                    else
                    {
                        match = false;
                        break;
                    }
                }
                if (match)
                {
                    return (IZabbixTask) constructor.newInstance(paramValues);
                }
            }
        }
        throw new NoSuchMethodException("No suitable constructor found for task: " + taskClassName);
    }

    private static Object convertToType(Class<?> type, Object value)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
    {
        if (type.isInstance(value))
        {
            return value;
        }
        if (type.isPrimitive())
        {
            // Handle primitive types
            if (type == int.class)
            {
                return Integer.parseInt(value.toString());
            }
            else if (type == boolean.class)
            {
                return Boolean.parseBoolean(value.toString());
            }
            else if (type == double.class)
            {
                return Double.parseDouble(value.toString());
            } // Add other primitive types as needed
        }
        else
        {
            try
            {
                // Try to use the valueOf(String) method if available
                Method valueOfMethod = type.getMethod("valueOf", String.class);
                return valueOfMethod.invoke(null, value.toString());
            }
            catch (NoSuchMethodException e)
            {
                // If valueOf is not available, try to use a constructor that takes a String
                Constructor<?> constructor = type.getConstructor(String.class);
                return constructor.newInstance(value.toString());
            }
        }
        return value; // Return the original value if no conversion is possible
    }
}

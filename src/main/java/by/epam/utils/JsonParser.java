package by.epam.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonParser
{
    public static <T> String jsonFromObject(T obj)
    {
        Gson gson =  new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(obj, obj.getClass());
    }

    public static <T> T objectFromJson(String json, Class<T> cls)
    {
        Gson gson = new Gson();
        return gson.fromJson(json, cls);
    }
}

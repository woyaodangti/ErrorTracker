package com.mike.errortracker.tracker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * description:
 * gson为线程安全的类，所以可以使用工具类来减少对象的实例化
 *
 * @author wkGui
 */
public class GsonUtil {
    private static final Gson filterNullGson;
    private static final Gson nullableGson;
    static {
        nullableGson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
                .create();
        filterNullGson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
                .create();
    }

    protected GsonUtil() {
    }

    /**
     * 根据对象返回json   不过滤空值字段
     */
    public static String toJsonWtihNullField(Object obj){
        return nullableGson.toJson(obj);
    }

    /**
     * 根据对象返回json  过滤空值字段
     */
    public static String toJsonFilterNullField(Object obj){
        return filterNullGson.toJson(obj);
    }

    /**
     * 将json转化为对应的实体对象
     * new TypeToken<HashMap<String, Object>>(){}.getType()
     */
    public static <T>  T fromJson(String json, Type type){
        return nullableGson.fromJson(json, type);
    }
}
package com.zebra.carcloud.dubboExt.filter.cat;

import com.dianping.cat.Cat;

import java.util.HashMap;
import java.util.Map;

public class CatContext implements Cat.Context {
    private Map<String,String> properties = new HashMap<String,String>();
    @Override
    public String getProperty(String s) {
        return properties.get(s);
    }

    @Override
    public void addProperty(String s, String s1) {
        properties.put(s,s1);
    }
}
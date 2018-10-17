package bymihaj.jvm;

import com.google.gson.Gson;

import bymihaj.IJsonParser;

public class GsonParser implements IJsonParser {

    public Gson gson = new Gson();
    
    @Override
    public <T> T fromJson(String json, Class<?> classOfT) {
        return gson.<T>fromJson(json, classOfT);
    }

    @Override
    public String toJson(Object src) {
        return gson.toJson(src);
    }

}

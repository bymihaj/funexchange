package bymihaj;

public interface IJsonParser {
    public <T> T fromJson(String json, Class<?> classOfT);
    public String toJson(Object src);
}

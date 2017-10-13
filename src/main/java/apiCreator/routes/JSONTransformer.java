package apiCreator.routes;

import com.google.gson.Gson;
import spark.ResponseTransformer;

public class JSONTransformer implements ResponseTransformer {

    private static final Gson gson = new Gson();

    public static String toJSON(Object model) {
        return gson.toJson(model);
    }

    public static Object fromJSON(String json, Class target) {
        return gson.fromJson(json, target);
    }

    @Override
    public String render(Object model) throws Exception {
        return toJSON(model);
    }
}
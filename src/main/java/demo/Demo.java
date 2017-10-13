package demo;

import apiCreator.routes.CRUDRoutes;
import apiCreator.updates.Field;
import demo.database.Mongo;
import demo.model.GreenFee;

import static demo.database.DatabaseSchema.GREEN_FEE_COLLECTION;

public class Demo {

    public static void main(String[] args) {
        Mongo mongoServer = new Mongo("mongodb://localhost:27017");

        new CRUDRoutes(
                mongoServer.getDB().getCollection(GREEN_FEE_COLLECTION),
                API_V1_PREFIX,
                API_HOST,
                RESOURCE_NAME,
                GreenFee.class,
                new Field(PRICE_PARAM_NAME, true, true),
                new Field(TYPE_PARAM_NAME, true, true)
        );
    }

    private static String API_V1_PREFIX = "/api/1/";
    private static String RESOURCE_NAME = "green-fee";
    private static String API_HOST = "https://api.golficy.com";

    private static String PRICE_PARAM_NAME = "price";
    private static String TYPE_PARAM_NAME = "type";

}

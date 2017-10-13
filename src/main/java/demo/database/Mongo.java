package demo.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import static demo.database.DatabaseSchema.DATABASE;

public class Mongo {

    public Mongo(String connectionURL) {
        MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionURL));
        database = mongoClient.getDatabase(DATABASE);
    }

    public MongoDatabase getDB() {
        return database;
    }


    private MongoDatabase database;
}

package apiCreator.handlers;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import apiCreator.updates.Field;
import apiCreator.updates.Resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.MissingResourceException;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static apiCreator.logging.APICreator.logger;
import static apiCreator.routes.JSONTransformer.fromJSON;

public class CRUDHandler {

    public CRUDHandler(MongoCollection<Document> collection, Class resource, Field[] fields) {
        this.collection = collection;
        this.resource = resource;
        this.fields = fields;
    }

    public Resource getResource(String id) {
        logger.info("[" + id + "] Getting " + resource.getSimpleName());
        Document doc = collection.find(eq(ID_FIELD, id)).first();
        if (doc == null)
            throw new MissingResourceException(
                    "Resource not found in MongoCollection",
                    resource.getClass().getSimpleName(),
                    id
            );
        return toResource(doc, id);
    }

    public Resource createResource(Resource newResource) {
        ObjectId objId = new ObjectId();
        Document newDoc = toDocument(newResource, objId);
        collection.insertOne(newDoc);
        return getResource(objId.toString());
    }

    public boolean deleteResource(String id) {
        getResource(id);
        return (collection.deleteOne(eq(ID_FIELD, id)).getDeletedCount() == 1);
    }

    public Resource updateField(Field field, Object newValue, String id) {
        if (!field.isUpdatable())
            //TODO: Make custom exception
            throw new UnsupportedOperationException();
        collection.findOneAndUpdate(eq(ID_FIELD, id), set(field.getName(), newValue));
        return getResource(id);
    }

    private Resource toResource(Document doc, String id) {
        Resource res = (Resource) fromJSON(doc.toJson(), resource);
        res.setID(id);
        return res;
    }

    private Document toDocument(Resource resource, ObjectId objID) {
        Document doc = new Document();
        for (Field field : fields) {
            Object fieldValue = getValueForField(resource, field);
            if (fieldValue == null && field.isRequired())
                //TODO: Make custom exception
                throw new MissingResourceException(
                        "Missing field in resource: " + field.getName(),
                        resource.getClass().getSimpleName(),
                        field.getName()
                );
            doc.append(field.getName(), fieldValue);
        }
        doc.append(ID_FIELD, objID.toString());
        return doc;
    }

    private Object getValueForField(Resource resource, Field field) {
        Object toReturn = null;
        try {
            Method method = resource.getClass().getMethod(GET + format(field.getName()));
            toReturn = method.invoke(resource);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    private String format(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    private MongoCollection<Document> collection;
    private Class resource;
    private Field[] fields;

    private final String ID_FIELD = "_id";
    private final String GET = "get";

}

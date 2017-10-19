package apiCreator.handlers;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import apiCreator.updates.Field;
import apiCreator.updates.Resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.MissingResourceException;

import static apiCreator.constants.LoggingConstants.*;
import static apiCreator.constants.HandlerConstants.*;
import static apiCreator.logging.APICreatorLogging.*;
import static apiCreator.utils.StringFormatter.fs;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static apiCreator.routes.JSONTransformer.fromJSON;

public class CRUDHandler {

    public CRUDHandler(MongoCollection<Document> collection, Class resource, Field[] fields) {
        this.collection = collection;
        this.resource = resource;
        this.fields = fields;
    }

    public Resource getResource(String id) {
        logger.info(fs("[", id, "] Getting ", resource.getSimpleName()));
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
        logger.info(fs("Creating new ", this.resource.getSimpleName()));
        ObjectId objId = new ObjectId();
        Document newDoc = toDocument(newResource, objId);
        collection.insertOne(newDoc);
        return getResource(objId.toString());
    }

    public boolean deleteResource(String id) {
        logger.info(fs("[", id, "] Deleting ", resource.getSimpleName()));
        getResource(id);
        return (collection.deleteOne(eq(ID_FIELD, id)).getDeletedCount() == 1);
    }

    public Resource updateField(Field field, Object newValue, String id) {
        logger.info(fs("[", id, "] Updating field ", field.getName(), " in ", resource.getSimpleName()));
        if (!field.isUpdatable())
            //TODO: Make custom exception
            throw new UnsupportedOperationException();
        collection.findOneAndUpdate(eq(ID_FIELD, id), set(field.getName(), newValue));
        return getResource(id);
    }

    private Resource toResource(Document doc, String id) {
        logger.debug(fs("[", id, "] Converting to ", resource.getSimpleName()));
        Resource res = (Resource) fromJSON(doc.toJson(), resource);
        res.setID(id);
        return res;
    }

    private Document toDocument(Resource resource, ObjectId objID) {
        logger.debug(fs("[", objID.toString(), "] Converting ", this.resource.getSimpleName(), " to Document"));
        Document doc = new Document();
        for (Field field : fields) {
            Object fieldValue = getValueForField(resource, field);
            if (fieldValue == null && field.isRequired())
                //TODO: Make custom exception
                throw new MissingResourceException(
                        fs("Missing field in resource: ", field.getName()),
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
            Method method = resource.getClass().getMethod(fs(GET_METHOD, format(field.getName())));
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
        return fs(name.substring(0, 1).toUpperCase(), name.substring(1).toLowerCase());
    }

    private MongoCollection<Document> collection;
    private Class resource;
    private Field[] fields;
}

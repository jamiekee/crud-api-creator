package apiCreator.routes;

import com.mongodb.client.MongoCollection;
import apiCreator.handlers.CRUDHandler;
import apiCreator.logging.APICreator;
import apiCreator.model.Link;
import org.bson.Document;
import org.eclipse.jetty.http.HttpStatus;
import apiCreator.updates.Field;
import apiCreator.updates.Resource;

import java.util.HashMap;
import java.util.Map;

import static apiCreator.api.Constants.*;
import static apiCreator.routes.JSONTransformer.fromJSON;
import static spark.Spark.*;

public class CRUDRoutes {

    public CRUDRoutes(
            MongoCollection<Document> collection, String apiPrefix, String apiHost,
            String resourceName, Class resource, Field... updatableFields
    ) {
        crudHandler = new CRUDHandler(collection, resource, updatableFields);
        this.apiPrefix = apiPrefix;
        this.apiHost = apiHost;
        this.resourceName = resourceName;
        this.resourceURI = this.resourceName + SLASH + ID_PARAM;
        this.updatableFields = updatableFields;
        this.resource = resource;
        _links = new HashMap<>();
        loadRoutes();
    }

    private void loadRoutes() {

        path(apiPrefix, () -> {

            String getURI = apiPrefix + resourceURI;
            APICreator.logger.info(NEW_ENDPOINT + GET + getURI);
            _links.put(SELF_LINK, new Link(apiHost + getURI, HTTP_GET));
            get(resourceURI, ((request, response) ->
                    addLinks(
                            crudHandler.getResource(request.params(ID_PARAM_NAME)),
                            request.params(ID_PARAM_NAME)
                    )),
                    new JSONTransformer());

            String createURI = apiPrefix + resourceName;
            APICreator.logger.info(NEW_ENDPOINT + POST + createURI);
            _links.put(CREATE_LINK, new Link(apiHost + createURI, HTTP_POST));
            post(resourceName, ((request, response) -> {
                        Resource res = crudHandler.createResource((Resource) fromJSON(request.body(), resource));
                        return addLinks(res, res.getResourceID());
                    }),
                    new JSONTransformer());

            String deleteURI = apiPrefix + resourceURI;
            APICreator.logger.info(NEW_ENDPOINT + DELETE + deleteURI);
            _links.put(DELETE_LINK, new Link(apiHost + deleteURI, HTTP_DELETE));
            delete(resourceURI, ((request, response) -> {
                if (!crudHandler.deleteResource(request.params(ID_PARAM_NAME)))
                    response.status(HttpStatus.NOT_MODIFIED_304);
                return EMPTY;
            }));

            for (Field putField : updatableFields) {
                if (putField.isUpdatable()) {
                    String uri = resourceURI + SLASH + putField.getName();
                    APICreator.logger.info(NEW_ENDPOINT + PUT + apiPrefix + uri);
                    _links.put(
                            UPDATE_LINK + putField.getName().toLowerCase(),
                            new Link(apiHost + apiPrefix + uri, HTTP_PUT)
                    );
                    put(uri, ((request, response) -> {
                        Map<String, String> map = (Map<String, String>) fromJSON(request.body(), Map.class);
                        Resource res = crudHandler.updateField(
                                putField, map.get(putField.getName()), request.params(ID_PARAM_NAME)
                        );
                        return addLinks(res, res.getResourceID());
                    }), new JSONTransformer());
                }
            }

        });

        after((request, response) -> response.type(JSON_RESPONSE));
    }

    private Resource addLinks(Resource resource, String id) {
        HashMap<String, Link> links = (HashMap<String, Link>)_links.clone();
        links.forEach((key, value) ->
                value.setURL(value.getURL().replaceAll(ID_PARAM, id)));
        resource.setLinks(_links);
        return resource;
    }

    private CRUDHandler crudHandler;
    private String resourceName;
    private String apiPrefix;
    private String apiHost;
    private String resourceURI;
    private Field[] updatableFields;
    private Class resource;
    private HashMap<String, Link> _links;

    private static final String SLASH = "/";
    private static final String COLON = ":";
    public static final String ID_PARAM = ":id";
    private static final String ID_PARAM_NAME = "id";

    private static final String NEW_ENDPOINT = "[New Endpoint]";
    private static final String GET = "[GET] ";
    private static final String POST = "[POST] ";
    private static final String DELETE = "[DELETE] ";
    private static final String PUT = "[PUT] ";
}

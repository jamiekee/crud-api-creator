package apiCreator.routes;

import com.mongodb.client.MongoCollection;
import apiCreator.handlers.CRUDHandler;
import apiCreator.model.Error;
import apiCreator.model.Link;
import org.bson.Document;
import org.eclipse.jetty.http.HttpStatus;
import apiCreator.updates.Field;
import apiCreator.updates.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import static apiCreator.constants.APIConstants.*;
import static apiCreator.constants.LinkConstants.*;
import static apiCreator.constants.LoggingConstants.*;
import static apiCreator.logging.APICreatorLogging.logger;
import static apiCreator.routes.JSONTransformer.fromJSON;
import static apiCreator.routes.JSONTransformer.toJSON;
import static apiCreator.utils.StringFormatter.fs;
import static spark.Spark.*;

public class CRUDRoutes {

    public CRUDRoutes(
            MongoCollection<Document> collection, String apiPrefix, String apiHost,
            String resourceName, Class resource, Field... fields
    ) {
        crudHandler = new CRUDHandler(collection, resource, fields);
        this.apiPrefix = apiPrefix;
        this.apiHost = apiHost;
        this.resourceName = resourceName;
        this.resourceURI = fs(resourceName, SLASH, ID_PARAM);
        this.fields = fields;
        this.resource = resource;
        _links = new HashMap<>();
        loadRoutes();
    }

    private void loadRoutes() {

        path(apiPrefix, () -> {

            String getURI = fs(apiPrefix, resourceURI);
            logger.info(fs(NEW_ENDPOINT, GET, getURI));
            _links.put(SELF_LINK, new Link(fs(apiHost, getURI), HTTP_GET));
            get(resourceURI, ((request, response) ->
                    addLinks(
                            crudHandler.getResource(request.params(ID_PARAM_NAME)),
                            request.params(ID_PARAM_NAME)
                    )),
                    new JSONTransformer());

            String createURI = fs(apiPrefix, resourceName);
            logger.info(fs(NEW_ENDPOINT, POST, createURI));
            _links.put(CREATE_LINK, new Link(fs(apiHost, createURI), HTTP_POST));
            post(resourceName, ((request, response) -> {
                        Resource res = crudHandler.createResource((Resource) fromJSON(request.body(), resource));
                        return addLinks(res, res.getResourceID());
                    }),
                    new JSONTransformer());

            String deleteURI = fs(apiPrefix, resourceURI);
            logger.info(fs(NEW_ENDPOINT, DELETE, deleteURI));
            _links.put(DELETE_LINK, new Link(fs(apiHost, deleteURI), HTTP_DELETE));
            delete(resourceURI, ((request, response) -> {
                if (!crudHandler.deleteResource(request.params(ID_PARAM_NAME)))
                    response.status(HttpStatus.NOT_MODIFIED_304);
                return EMPTY;
            }));

            for (Field putField : fields) {
                if (putField.isUpdatable()) {
                    String uri = fs(resourceURI, SLASH, putField.getName());
                    logger.info(fs(NEW_ENDPOINT, PUT, apiPrefix, uri));
                    _links.put(
                            fs(UPDATE_LINK, putField.getName().toLowerCase()),
                            new Link(fs(apiHost, apiPrefix, uri), HTTP_PUT)
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

        exception(MissingResourceException.class, (e, request, response) -> {
            logger.info(fs("[", request.uri(), "] Caught 404."));
            response.status(HttpStatus.NOT_FOUND_404);
            response.type(JSON_RESPONSE);
            response.body(toJSON(new Error(fs("URI not found."))));
        });

        exception(Exception.class, (e, request, response) -> {
            logger.info(fs("[", request.uri(), "] Caught 500."));
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            response.type(JSON_RESPONSE);
            response.body(toJSON(new Error(fs("Server error."))));
        });

        after((request, response) -> response.type(JSON_RESPONSE));
    }

    private Resource addLinks(Resource resource, String id) {
        HashMap<String, Link> links = (HashMap<String, Link>) _links.clone();
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
    private Field[] fields;
    private Class resource;
    private HashMap<String, Link> _links;
}

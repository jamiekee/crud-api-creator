package apiCreator.updates;

import apiCreator.model.Link;

import java.util.HashMap;

public interface Resource {

    String getResourceID();

    void setID(String id);

    void setLinks(HashMap<String, Link> links);

}

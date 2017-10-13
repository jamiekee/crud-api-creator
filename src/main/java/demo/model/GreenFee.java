package demo.model;

import apiCreator.model.Link;
import demo.identifiers.GreenFeeID;
import apiCreator.updates.Resource;

import java.util.HashMap;

public class GreenFee implements Resource {

    public GreenFeeID getGreenFeeID() {
        return greenFeeID;
    }

    public String getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public String getResourceID() {
        return greenFeeID.getID();
    }

    @Override
    public void setID(String id) {
        this.greenFeeID = new GreenFeeID(id);
    }

    @Override
    public void setLinks(HashMap<String, Link> links) {
        _links = links;
    }

    private GreenFeeID greenFeeID;
    private String type;
    private int price;
    private HashMap<String, Link> _links;
}


package apiCreator.model;

public class Link {

    public Link(String URL, String method) {
        this.URL = URL;
        this.method = method;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String url) {
        this.URL = url;
    }

    public String getMethod() {
        return method;
    }

    private String URL;
    private String method;
}

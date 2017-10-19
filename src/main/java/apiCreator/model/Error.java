package apiCreator.model;

public class Error {

    public Error(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    private String message;
}
package apiCreator.updates;

public class Field {

    public Field(String name, boolean isUpdatable, boolean isRequired) {
        this.name = name;
        this.isUpdatable = isUpdatable;
        this.isRequired = isRequired;
    }

    public String getName() {
        return name;
    }

    public boolean isUpdatable() {
        return isUpdatable;
    }

    public boolean isRequired() {
        return isRequired;
    }

    private String name;
    private boolean isUpdatable;
    private boolean isRequired;
}

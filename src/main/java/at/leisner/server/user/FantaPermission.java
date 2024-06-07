package at.leisner.server.user;

public class FantaPermission implements Permission {
    private String name;
    private boolean value;

    public FantaPermission(String name, boolean value) {
        this.name = name;
        this.value = value;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public boolean getValue() {
        return value;
    }
    @Override
    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FantaPermission that = (FantaPermission) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

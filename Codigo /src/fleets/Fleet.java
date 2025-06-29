package fleets;

public class Fleet {
    private int id;
    private String name;
    private int size;
    private String admiralName;

    public Fleet(int id, String name, int size, String admiralName) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.admiralName = admiralName;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getSize() { return size; }
    public String getAdmiralName() { return admiralName; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSize(int size) { this.size = size; }
    public void setAdmiralName(String admiralName) { this.admiralName = admiralName; }
}

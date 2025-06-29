package ships;

public class Ship {
    private int id;
    private String name;
    private int size;
    private String fleetName;

    public Ship(int id, String name, int size, String fleetName) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.fleetName = fleetName;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getSize() { return size; }
    public String getFleetName() { return fleetName; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSize(int size) { this.size = size; }
    public void setFleetName(String fleetName) { this.fleetName = fleetName; }
}

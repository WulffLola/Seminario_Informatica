package stations;

public class Station {
    private int id;
    private String personName;
    private String shipName;
    private String rankName;
    private String startDate;
    private String endDate;

    public Station(int id, String personName, String shipName, String rankName, String startDate, String endDate) {
        this.id = id;
        this.personName = personName;
        this.shipName = shipName;
        this.rankName = rankName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() { return id; }
    public String getPersonName() { return personName; }
    public String getShipName() { return shipName; }
    public String getRankName() { return rankName; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }

    public void setId(int id) { this.id = id; }
    public void setPersonName(String personName) { this.personName = personName; }
    public void setShipName(String shipName) { this.shipName = shipName; }
    public void setRankName(String rankName) { this.rankName = rankName; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}

package people;

public class Person {
    private int id;
    private String name;
    private String lastName;
    private String dni;
    private String birthdate; // String "YYYY-MM-DD" para simplificar
    private String rankName;  // nombre del rango

    public Person(int id, String name, String lastName, String dni, String birthdate, String rankName) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.dni = dni;
        this.birthdate = birthdate;
        this.rankName = rankName;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getBirthdate() { return birthdate; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }

    public String getRankName() { return rankName; }
    public void setRankName(String rankName) { this.rankName = rankName; }

    @Override
    public String toString() {
        return name + " " + lastName + " - " + rankName;
    }
}

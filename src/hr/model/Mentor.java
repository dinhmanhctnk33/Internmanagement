package hr.model;

public class Mentor {

    private int id;
    private String name;
    private String email;
    private String department;
    private boolean active;

    public Mentor(int id, String name, String email,
                  String department, boolean active) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public boolean isActive() {
        return active;
    }
}
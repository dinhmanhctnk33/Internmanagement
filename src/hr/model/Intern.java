package hr.model;

public class Intern {

    private int id;
    private String name;
    private String email;
    private String department;
    private String status;
    private Mentor mentor;

    public Intern(int id, String name, String email,
                  String department, String status) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.status = status;
        this.mentor = null;
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

    public String getStatus() {
        return status;
    }

    public Mentor getMentor() {
        return mentor;
    }

    public void setMentor(Mentor mentor) {
        this.mentor = mentor;
    }
}
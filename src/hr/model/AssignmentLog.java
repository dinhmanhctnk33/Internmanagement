package hr.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AssignmentLog {

    private String hrName;
    private Intern intern;
    private Mentor mentor;
    private Mentor oldMentor;
    private LocalDateTime time;

    // Log khi phan cong mentor lan dau
    public AssignmentLog(
            String hrName,
            Intern intern,
            Mentor mentor) {

        this.hrName = hrName;
        this.intern = intern;
        this.mentor = mentor;
        this.oldMentor = null;
        this.time = LocalDateTime.now();
    }

    // Log khi doi mentor
    public AssignmentLog(
            String hrName,
            Intern intern,
            Mentor oldMentor,
            Mentor newMentor) {

        this.hrName = hrName;
        this.intern = intern;
        this.oldMentor = oldMentor;
        this.mentor = newMentor;
        this.time = LocalDateTime.now();
    }

    public String getHrName() {
        return hrName;
    }

    public Intern getIntern() {
        return intern;
    }

    public Mentor getMentor() {
        return mentor;
    }

    public Mentor getOldMentor() {
        return oldMentor;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getFormattedTime() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy HH:mm:ss"
                );

        return time.format(formatter);
    }

    public void printLog() {

        System.out.println("HR: " + hrName);
        System.out.println("Intern: " + intern.getName());

        if (oldMentor != null) {

            System.out.println(
                    "Thay doi mentor: "
                    + oldMentor.getName()
                    + " -> "
                    + mentor.getName()
            );

        } else {

            System.out.println(
                    "Mentor: " + mentor.getName()
            );
        }

        System.out.println(
                "Thoi gian: " + getFormattedTime()
        );

        System.out.println("---------------------------------");
    }
    
        }

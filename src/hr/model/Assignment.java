package hr.model;

public class Assignment {

    private Intern intern;
    private Mentor proposedMentor;

    public Assignment(Intern intern, Mentor proposedMentor) {
        this.intern = intern;
        this.proposedMentor = proposedMentor;
    }

    public Intern getIntern() {
        return intern;
    }

    public Mentor getProposedMentor() {
        return proposedMentor;
    }

    public void setProposedMentor(Mentor proposedMentor) {
        this.proposedMentor = proposedMentor;
    }
}
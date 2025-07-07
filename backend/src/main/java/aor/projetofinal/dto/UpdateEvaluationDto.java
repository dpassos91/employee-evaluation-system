package aor.projetofinal.dto;

public class UpdateEvaluationDto {
    private int grade;                // the frontend should send a number between 1 and 4
    private String feedback;         // manager's feedback on the evaluation
    private String evaluatedEmail;   // evaluated user's email
    private String evaluatedName;

    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getEvaluatedEmail() { return evaluatedEmail; }
    public void setEvaluatedEmail(String evaluatedEmail) { this.evaluatedEmail = evaluatedEmail; }

    public String getEvaluatedName() { return evaluatedName; }
    public void setEvaluatedName(String evaluatedName) { this.evaluatedName = evaluatedName; }
}

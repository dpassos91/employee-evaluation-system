package aor.projetofinal.dto;

public class EvaluationOptionsDto {

    private String enumName;
    private int grade;
    private String label;

    public EvaluationOptionsDto(String enumName, int grade, String label) {
        this.enumName = enumName;
        this.grade = grade;
        this.label = label;
    }

    public String getEnumName() {
        return enumName;
    }

    public int getGrade() {
        return grade;
    }

    public String getLabel() {
        return label;
    }


}

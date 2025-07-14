package aor.projetofinal.dto;


/**
 * DTO representing an evaluation option for display in dropdowns or selection components.
 * Contains the enum name, numeric grade, and a formatted label combining grade and description.
 */
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

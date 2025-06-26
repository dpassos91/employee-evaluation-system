package aor.projetofinal.entity.enums;

public enum GradeEvaluationType {


//applying class/enum constructors and
// private fields for being able to add a description and a grade for each enum object

    CONTRIBUICAO_BAIXA(1, "Contribuição Baixa"),
    CONTRIBUICAO_PARCIAL(2, "Contribuição Parcial"),
    CONFORME_ESPERADO(3, "Contribuição conforme o Esperado"),
    CONTRIBUICAO_EXCEDIDA(4, "Contribuição Excedida");


    //private fields
    private final int grade;
    private final String description;

    //constructor for the enum and its respective grade and description
    GradeEvaluationType(int grade, String description) {
        this.grade = grade;
        this.description = description;
    }


    public int getGrade() {
        return grade;
    }

    public String getDescription() {
        return description;
    }


}

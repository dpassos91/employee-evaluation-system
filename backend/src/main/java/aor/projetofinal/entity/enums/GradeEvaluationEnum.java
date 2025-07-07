package aor.projetofinal.entity.enums;

public enum GradeEvaluationEnum {


//applying class/enum constructors and
// private fields for being able to add a description and a grade for each enum object

    LOW_CONTRIBUTION(1, "Low Contribution"),
    PARTIAL_CONTRIBUTION(2, "Partial Contribution"),
    AS_EXPECTED(3, "Contribution as Expected"),
    EXCEEDED_CONTRIBUTION(4, "Exceeded Contribution");


    //private fields
    private final int grade;
    private final String description;

    //constructor for the enum and its respective grade and description
    GradeEvaluationEnum(int grade, String description) {
        this.grade = grade;
        this.description = description;
    }


    public int getGrade() {
        return grade;
    }

    public String getDescription() {
        return description;
    }


    public static GradeEvaluationEnum getEnumfromGrade(int grade) {
        //values is a method that returns an array of all enum constants, that will be cycled through
        for (GradeEvaluationEnum option : GradeEvaluationEnum.values()) {
            if (option.getGrade() == grade) {
                return option;
            }
        }
        throw new IllegalArgumentException("Invalid grade received: " + grade);
    }


    public static GradeEvaluationEnum getEnumFromDescription(String description) {
        for (GradeEvaluationEnum option : GradeEvaluationEnum.values()) {
            if (option.description.equalsIgnoreCase(description)) {
                return option;
            }
        }
        throw new IllegalArgumentException("DInvalid description received: " + description);
    }





}

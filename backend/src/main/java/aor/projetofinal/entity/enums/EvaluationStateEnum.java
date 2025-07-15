package aor.projetofinal.entity.enums;

public enum EvaluationStateEnum {
    IN_EVALUATION,
    EVALUATED,
    CLOSED,;


    public static String transformToString(EvaluationStateEnum state) {
        if (state == null) throw new IllegalArgumentException("UsualWorkPlaceType cannot be null.");
        return state.name();
    }


}

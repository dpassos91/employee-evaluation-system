package aor.projetofinal.dto;

import java.util.List;

/**
 * Wrapper DTO for sending a list of evaluation states.
 */
public class EvaluationStatesDto {
    private List<EvaluationStateDto> states;

    public EvaluationStatesDto() {}

    public EvaluationStatesDto(List<EvaluationStateDto> states) {
        this.states = states;
    }

    public List<EvaluationStateDto> getStates() {
        return states;
    }

    public void setStates(List<EvaluationStateDto> states) {
        this.states = states;
    }
}

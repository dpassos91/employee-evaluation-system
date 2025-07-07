package aor.projetofinal.dto;

/**
 * DTO representing a single evaluation state option for dropdowns or filters.
 * Contains the enum name and a human-readable label.
 */
public class EvaluationStateDto {

    private String name;   // Enum name (e.g. "CLOSED")
    private String label;  // Human-readable label (e.g. "Closed")

    public EvaluationStateDto() {}

    public EvaluationStateDto(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}

package aor.projetofinal.dto;


import java.time.LocalDate;

/**
 * DTO used to receive data when creating a new evaluation cycle.
 * Currently, it contains only the end date of the cycle.
 */
public class CreateCycleDto {

    private LocalDate endDate;

    public CreateCycleDto() {
    }

    public CreateCycleDto(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "CreateCycleDto{" +
                "endDate=" + endDate +
                '}';
    }


}

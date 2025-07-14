package aor.projetofinal.dto;

/**
 * DTO that represents the total training hours for a user in a specific year.
 */
public class UserCourseYearSummaryDto {
    private int year;
    private double totalHours;

    public UserCourseYearSummaryDto() {}

    public UserCourseYearSummaryDto(int year, double totalHours) {
        this.year = year;
        this.totalHours = totalHours;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }
}

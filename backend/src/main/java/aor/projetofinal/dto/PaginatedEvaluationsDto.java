package aor.projetofinal.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.List;

/**
 * PaginatedEvaluationsDto is a container DTO for paginated responses of evaluation listings.
 * It contains a list of FlatEvaluationDto along with pagination metadata.
 */
@XmlRootElement
public class PaginatedEvaluationsDto implements Serializable {

    private List<FlatEvaluationDto> evaluations;
    private long totalCount;
    private int totalPages;
    private int currentPage;

    public PaginatedEvaluationsDto() {
    }

    public PaginatedEvaluationsDto(List<FlatEvaluationDto> evaluations, long totalCount, int totalPages, int currentPage) {
        this.evaluations = evaluations;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    @XmlElement
    public List<FlatEvaluationDto> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<FlatEvaluationDto> evaluations) {
        this.evaluations = evaluations;
    }

    @XmlElement
    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    @XmlElement
    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    @XmlElement
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}

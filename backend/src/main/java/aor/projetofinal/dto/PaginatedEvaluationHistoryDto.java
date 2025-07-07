package aor.projetofinal.dto;

import java.io.Serializable;
import java.util.List;

/**
 * PaginatedEvaluationHistoryDto holds a paginated response of the user's closed evaluations,
 * used for listing historical evaluation data.
 */
public class PaginatedEvaluationHistoryDto implements Serializable {

    private List<FlatEvaluationHistoryDto> evaluations;
    private long totalCount;
    private int totalPages;
    private int currentPage;

    public PaginatedEvaluationHistoryDto() {
    }

    public PaginatedEvaluationHistoryDto(List<FlatEvaluationHistoryDto> evaluations, long totalCount, int totalPages, int currentPage) {
        this.evaluations = evaluations;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    public List<FlatEvaluationHistoryDto> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<FlatEvaluationHistoryDto> evaluations) {
        this.evaluations = evaluations;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}

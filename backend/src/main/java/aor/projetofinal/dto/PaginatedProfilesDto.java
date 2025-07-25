package aor.projetofinal.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;


/**
 * DTO representing a paginated result set of profile data.
 * Contains the list of profiles and pagination metadata such as total count, pages, and current page.
 */
@XmlRootElement
public class PaginatedProfilesDto {
    private List<FlatProfileDto> profiles;
    private long totalCount;
    private int totalPages;
    private int currentPage;

    public PaginatedProfilesDto() {
    }

    public PaginatedProfilesDto(List<FlatProfileDto> profiles, long totalCount, int totalPages, int currentPage) {
        this.profiles = profiles;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    // Getters e Setters
    @XmlElement
    public List<FlatProfileDto> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<FlatProfileDto> profiles) {
        this.profiles = profiles;
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

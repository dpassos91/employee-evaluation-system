package aor.projetofinal.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement
public class PaginatedProfilesDto {
    private List<ProfileDto> profiles;
    private long totalCount;
    private int totalPages;
    private int currentPage;

    public PaginatedProfilesDto() {
    }

    public PaginatedProfilesDto(List<ProfileDto> profiles, long totalCount, int totalPages, int currentPage) {
        this.profiles = profiles;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    // Getters e Setters
    @XmlElement
    public List<ProfileDto> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<ProfileDto> profiles) {
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

package aor.projetofinal.dto;

import java.util.List;


/**
 * DTO representing a paginated list of users without managers,
 * along with pagination metadata such as total count, current page, and page size.
 */
public class PaginatedUsersDto {
    private List<NoManagerDto> users;
    private long total;
    private int page;
    private int pageSize;

    public List<NoManagerDto> getUsers() {
        return users;
    }

    public void setUsers(List<NoManagerDto> users) {
        this.users = users;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}

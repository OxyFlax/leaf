package fr.iolabs.leaf.organization;

public class OrganizationSearchCriteria {
	private String name;
	private OrganizationSearchOrder orderBy = OrganizationSearchOrder.NAME;
	private int page = 0;
	private int pageSize = 10;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OrganizationSearchOrder getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(OrganizationSearchOrder orderBy) {
		this.orderBy = orderBy != null ? orderBy : OrganizationSearchOrder.NAME;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = Math.max(0, page);
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize > 0 ? pageSize : 10;
	}
}

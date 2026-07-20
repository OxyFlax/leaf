package fr.iolabs.leaf.organization;

import java.util.List;

import fr.iolabs.leaf.organization.model.LeafOrganization;

public class OrganizationSearchResponse {
	private List<LeafOrganization> organizations;
	private long totalCount;
	private int pageCount;
	private int currentPage;

	public OrganizationSearchResponse() {
	}

	public OrganizationSearchResponse(List<LeafOrganization> organizations, long totalCount, int pageCount,
			int currentPage) {
		this.organizations = organizations;
		this.totalCount = totalCount;
		this.pageCount = pageCount;
		this.currentPage = currentPage;
	}

	public List<LeafOrganization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<LeafOrganization> organizations) {
		this.organizations = organizations;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
}

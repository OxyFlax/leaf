package fr.iolabs.leaf.authentication.read;

import java.util.List;

import fr.iolabs.leaf.authentication.model.LeafAccount;

public class AccountSearchResponse {
	private List<LeafAccount> accounts;
	private long totalCount;
	private int pageCount;
	private int currentPage;

	public AccountSearchResponse() {
	}

	public AccountSearchResponse(List<LeafAccount> accounts, long totalCount, int pageCount, int currentPage) {
		this.accounts = accounts;
		this.totalCount = totalCount;
		this.pageCount = pageCount;
		this.currentPage = currentPage;
	}

	public List<LeafAccount> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<LeafAccount> accounts) {
		this.accounts = accounts;
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

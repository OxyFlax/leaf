package fr.iolabs.leaf.authentication.read;

public class AccountSearchCriteria {
	private String email;
	private AccountSearchOrder orderBy = AccountSearchOrder.FIRST_REGISTERED;
	private int page = 0;
	private int pageSize = 10;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public AccountSearchOrder getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(AccountSearchOrder orderBy) {
		this.orderBy = orderBy != null ? orderBy : AccountSearchOrder.FIRST_REGISTERED;
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

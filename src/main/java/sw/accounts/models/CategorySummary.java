package sw.accounts.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class CategorySummary {

	private String category;
	private float total;

	public void addToTotal(Transaction aTrans)
	{
		this.total += aTrans.getAmount();
	}

	public String getParentCategory() {
		final String s = this.getCategory();
		final int ipos = s.lastIndexOf(':');
		return (ipos > -1) ? s.substring(0, ipos).trim() : null;
	}

}

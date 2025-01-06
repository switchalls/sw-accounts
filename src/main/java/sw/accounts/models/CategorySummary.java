package sw.accounts.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import org.springframework.util.StringUtils;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class CategorySummary {

	private String category;
	private float total;

	public boolean isRootCategory() {
		return isValidCategory() && !this.category.contains(":");
	}
	
	public boolean isValidCategory() {
		return StringUtils.hasLength(this.category) && !Transaction.SPLIT.equals(this.category);
	}
	
	public void addToTotal(CategorySummary cs)
	{
		this.total += cs.getTotal();
	}

	public void addToTotal(Transaction t)
	{
		this.total += t.getAmount();
	}

	public String getParentCategory() {
		final String s = this.getCategory();
		final int ipos = s.lastIndexOf(':');
		return (ipos > -1) ? s.substring(0, ipos).trim() : null;
	}

}

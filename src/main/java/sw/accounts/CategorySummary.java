package sw.accounts;

import java.io.Serializable;

public class CategorySummary implements Serializable {

	private static final long serialVersionUID = 821136850083993683L;

	private String category;
	private float total;
	
	public String getCategory()
	{
		return this.category;
	}

	public void setCategory(String aName)
	{
		this.category = aName;
	}

	public float getTotal()
	{
		return this.total;
	}

	public void setTotal(float aTotal)
	{
		this.total = aTotal;
	}

	public void add(Transaction aTrans)
	{
		this.total += aTrans.getAmount();
	}

	public String getParentCategory() {
		final String s = this.getCategory();
		final int ipos = s.lastIndexOf(':');
		return (ipos > -1) ? s.substring(0, ipos).trim() : null;
	}

	public boolean isSubcategory(String aCategory) {
		return aCategory.startsWith( this.getCategory()+":" );
	}

}

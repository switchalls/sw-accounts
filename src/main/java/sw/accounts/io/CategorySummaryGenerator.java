package sw.accounts.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sw.accounts.CategorySummary;
import sw.accounts.Transaction;

public class CategorySummaryGenerator {

	private final List<CategorySummary> summaries = new ArrayList<>();
	
	public List<CategorySummary> getSummaries()
	{
		return this.summaries;
	}

	public void addTransactions(Collection<Transaction> aTransactions) {
		for ( Transaction t : aTransactions ) {
			String c = t.getCategory();
			while ( c != null ) {
				CategorySummary s = this.findSummary( c );
				if ( s == null ) {
					s = new CategorySummary();
					s.setCategory( c );
					this.summaries.add( s );
				}
				
				s.add( t );
				
				c = s.getParentCategory();
			}
		}
	}

	public CategorySummary findSummary(String aCategory) {
		for ( CategorySummary s : this.summaries ) {
			if ( s.getCategory().equals(aCategory) ) {
				return s;
			}
		}
		return null;
	}

}

package sw.accounts.io;

import sw.accounts.models.CategorySummary;
import sw.accounts.models.Transaction;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class CategorySummaryGenerator {

	public List<CategorySummary> addTransactions(Collection<Transaction> aTransactions) {
		final List<CategorySummary> summaries = new ArrayList<>();

		for ( Transaction t : aTransactions ) {
			String c = t.getCategory();
			while ( c != null ) {
				CategorySummary s = this.findSummary( summaries, c );
				if ( s == null ) {
					s = CategorySummary.builder()
							.category( c )
							.build();

					summaries.add( s );
				}
				
				s.addToTotal( t );
				
				c = s.getParentCategory();
			}
		}

		return summaries;
	}

	private CategorySummary findSummary(List<CategorySummary> summaries, String aCategory) {
		return summaries.stream()
				.filter((s) -> s.getCategory().equals(aCategory))
				.findFirst()
				.orElse(null);
	}

}

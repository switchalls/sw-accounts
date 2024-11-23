package sw.accounts.pocketmoney;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.springframework.stereotype.Component;
import sw.accounts.AbstractReportGenerator;
import sw.accounts.Transaction;

@Component
public class PocketMoneyReportGenerator extends AbstractReportGenerator {

	public List<Transaction> loadTransactions(
			File aTransactionsFile,
			Calendar aStartDate,
			Calendar aEndDate)
	throws	Exception {
		final PocketMoneyTransactionsReader reader = new PocketMoneyTransactionsReader();
		reader.setDateRange( aStartDate, aEndDate );
		reader.loadTransactions( aTransactionsFile );

		return reader.getTransactions();
	}

}

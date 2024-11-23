package sw.accounts.pocketmoney;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import sw.accounts.AbstractSummaryReportGenerator;
import sw.accounts.AccountsException;
import sw.accounts.Transaction;

public class PocketMoneySummaryReportGenerator extends AbstractSummaryReportGenerator {
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

	public static void main(String[] aArgs) {
		try {
			if ( aArgs.length != 3 ) {
				throw new AccountsException("Usage: PocketMoneySummaryReportGenerator <accounts.csv> <transactions.csv> <output.csv>" );
			}

			final PocketMoneySummaryReportGenerator generator = new PocketMoneySummaryReportGenerator();
			generator.loadAccounts( new File(aArgs[0]) );
			generator.loadTransactions( new File(aArgs[1]) );
			generator.updateAccounts();
			generator.generateSummaryReport( new File(aArgs[2]) );
			generator.persistAccounts( new File(aArgs[0]) );
		}
		catch (Exception e) {
			e.printStackTrace( System.err );
		}
	}

}

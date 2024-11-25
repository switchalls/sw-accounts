package sw.accounts.io.pocketmoney;

import sw.accounts.exceptions.AccountsException;
import sw.accounts.io.TransactionsReader;
import sw.accounts.models.Transaction;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class PocketMoneyTransactionsReader implements TransactionsReader {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "dd/MM/yyyy" );

	private final TransactionReader transactionReader;

	public PocketMoneyTransactionsReader(TransactionReader transactionReader) {
		this.transactionReader = transactionReader;
	}

	@Override
	public List<Transaction> loadTransactions(Calendar startDate, Calendar endDate, File... files) throws Exception {
		final List<Transaction> transactions = new ArrayList<>();

		for (File f : files) {
			try (InputStream is = Files.newInputStream(f.toPath())) {
				transactions.addAll( this.loadTransactions(startDate, endDate, is) );
			}
		}

		return transactions;
	}

	private List<Transaction> loadTransactions(Calendar startDate, Calendar endDate, InputStream is) throws AccountsException, IOException {
		final BufferedReader reader = new BufferedReader( new InputStreamReader(is) );
		final List<Transaction> newTransactions = new ArrayList<>();

		// skip headers

        reader.readLine();

		// read transactions

		Transaction splitParent = null;
		Transaction previous = null;
		int lineCount = 1;

		String s;
		while ( (s = reader.readLine()) != null ) {
			lineCount++;

			try {
				final Transaction t = this.addTransaction( startDate, endDate, s );

				if ( t != null ) {
					if ( splitParent != null ) {
						if ( t.isSplitOf(splitParent) ) {
							splitParent.setAmount( splitParent.getAmount() + t.getAmount() );
							t.setAsSplit();
						} else {
							splitParent = null;
						}
					} else if ( t.isSplitOf(previous)) {
						splitParent = previous.toBuilder()
								.amount( previous.getAmount() + t.getAmount() )
								.category( Transaction.SPLIT )
								.memo("")
								.build();

						newTransactions.add( newTransactions.size() - 1, splitParent );

						previous.setAsSplit();
						t.setAsSplit();
					}

					newTransactions.add( t );

					previous = t;
				}
			}
			catch (Exception e) {
				throw new AccountsException( "Cannot load pocket-money file: Error at line: "+lineCount, e );
			}
		}

		return newTransactions;
	}

	private Transaction addTransaction(Calendar startDate, Calendar endDate, String aContent) throws Exception {
		final Transaction t = this.transactionReader.loadTransaction(aContent);

		final Calendar c = GregorianCalendar.getInstance();
		c.setTime(t.getDate());

		if (c.before(startDate)) {
			System.out.println("Ignoring transaction before " + DATE_FORMAT.format(startDate.getTime()) + ": " + t);
			return null;
		}

		if ((endDate != null) && endDate.before(c)) {
			System.out.println("Ignoring transaction after " + DATE_FORMAT.format(endDate.getTime()) + ": " + t);
			return null;
		}

		return t;
	}
}

package sw.accounts.io.pocketmoney;

import sw.accounts.exceptions.AccountsException;
import sw.accounts.io.csv.CsvTokeniser;
import sw.accounts.io.TransactionsReader;
import sw.accounts.models.Transaction;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class PocketMoneyTransactionsReader implements TransactionsReader {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "dd/MM/yyyy" );

	@Override
	public List<Transaction> loadTransactions(Calendar startDate, Calendar endDate, File aPath) throws Exception {
		try (InputStream in = Files.newInputStream(aPath.toPath())) {
			return this.loadTransactions(startDate, endDate, in);
		}
	}

	private List<Transaction> loadTransactions(Calendar startDate, Calendar endDate, InputStream aIn) throws AccountsException, IOException {
		final BufferedReader reader = new BufferedReader( new InputStreamReader(aIn) );
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
		final Transaction t = this.loadTransaction(aContent);

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

	private Transaction loadTransaction(String aContent) throws Exception {
		final CsvTokeniser tokeniser = new CsvTokeniser( aContent );

		final Transaction.TransactionBuilder builder = Transaction.builder();

		final String account = tokeniser.nextToken("Account");
		final String tdate = tokeniser.nextToken("Date");
		final String tid = tokeniser.nextToken("ChkNum");
		final String payee = tokeniser.nextToken("Payee");

		builder.account( account );
		builder.payee( payee );
		builder.category( tokeniser.nextToken("Category") );
		builder.clazz( tokeniser.nextToken("Class") );

		final String memo = tokeniser.nextToken("Memo");
		final float amount = Float.parseFloat(tokeniser.nextToken("Amount").replaceAll(",", ""));

		builder.memo( memo );
		builder.amount( amount );

		builder.cleared(!tokeniser.nextToken("Cleared").isEmpty());

		tokeniser.nextToken("CurrencyCode");
		tokeniser.nextToken("ExchangeRate");
		tokeniser.nextToken("Balance");
		
		if ( tokeniser.hasMoreTokens() ) {
			throw new AccountsException("Too many pocket-money transaction fields");				
		}

		builder.date( DATE_FORMAT.parse(tdate) );

		if (!tid.isEmpty()) {
			if ( Character.isDigit(tid.charAt(0)) ) {
				builder.checkNumber( tid ).type( Transaction.CHEQUE );
			}
			else if ( tid.indexOf('#') < 0 ) {
				builder.type( tid );
			}
			else if ( "".equals(memo) ) {
				builder.memo( tid );
			}
			else {
				builder.memo( memo + " ["+tid+"]" );
			}
		}

		if ( payee.startsWith("<") && payee.endsWith(">") ) {
			final int plen = payee.length();
			final String tother =payee.substring( 1, plen-1 );
			
			if ( amount < 0 ) {
				// transfer source
				builder.payee( tother );
			}
			else {
				// transfer target
				builder.payee( account );
				builder.transferOther( tother );
			}

			builder.type( Transaction.ELECTRONIC_TRANSFER );
		}

		return builder.build();
	}

}

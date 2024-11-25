package sw.accounts.io.pocketmoney;

import sw.accounts.exceptions.AccountsException;
import sw.accounts.io.csv.CsvTokeniser;
import sw.accounts.models.Transaction;

import java.text.SimpleDateFormat;

public class CatamountSoftwareCsvReader implements TransactionReader {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "dd/MM/yyyy" );

	@Override
	public Transaction loadTransaction(String data) throws Exception {
		final CsvTokeniser tokeniser = new CsvTokeniser( data );

		final String account = tokeniser.nextToken("Account");
		final String tdate = tokeniser.nextToken("Date");
		final String tid = tokeniser.nextToken("ChkNum");
		final String payee = tokeniser.nextToken("Payee");
		final String category = tokeniser.nextToken("Category");
		final String clazz = tokeniser.nextToken("Class");
		final String memo = tokeniser.nextToken("Memo");
		final float amount = Float.parseFloat(tokeniser.nextToken("Amount").replaceAll(",", ""));
		final String cleared = tokeniser.nextToken("Cleared");

		final Transaction.TransactionBuilder builder = Transaction.builder()
			.account( account )
			.payee( payee )
			.amount( amount )
			.category( category )
			.clazz( clazz )
			.memo( memo )
			.cleared(!cleared.isEmpty());

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

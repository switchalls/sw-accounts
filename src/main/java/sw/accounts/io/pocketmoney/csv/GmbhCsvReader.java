package sw.accounts.io.pocketmoney.csv;

import sw.accounts.exceptions.AccountsException;
import sw.accounts.io.csv.CsvTokeniser;
import sw.accounts.io.pocketmoney.TransactionReader;
import sw.accounts.models.Transaction;

import java.text.SimpleDateFormat;

public class GmbhCsvReader implements TransactionReader {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "dd/MM/yyyy" );

	@Override
	public Transaction loadTransaction(String data) throws Exception {
		final CsvTokeniser tokeniser = new CsvTokeniser( data );

		final String account = tokeniser.nextToken("Account");
		final String status = tokeniser.nextToken("Status");
		final String tdate = tokeniser.nextToken("Date");
		final String name = tokeniser.nextToken("Name");
		final float amount = Float.parseFloat(tokeniser.nextToken("Amount"));
		final String number = tokeniser.nextToken("Number");
		final String memo = tokeniser.nextToken("Comment");
		final String category  = tokeniser.nextToken("Category");

		if ( tokeniser.hasMoreTokens() ) {
			throw new AccountsException("Too many pocket-money transaction fields");
		}

		final Transaction.TransactionBuilder builder = Transaction.builder()
				.account( account )
				.amount( amount )
				.date( DATE_FORMAT.parse(tdate) )
				.category( category.replaceAll(" : ", ":") )
				.cleared( "Cleared".equals(status) )
				.payee( name )
				.memo( memo );

		if (isNumber(number)) {
			builder.checkNumber(number).type(Transaction.CHEQUE);
		} else {
			builder.type( number );
		}

		if (name.startsWith("Transfer to")) {
			builder.transferOther( name.substring(12) ).type( Transaction.ELECTRONIC_TRANSFER );
		}

		if (name.startsWith("Transfer from")) {
			builder.transferOther( name.substring(14) ).type( Transaction.ELECTRONIC_TRANSFER );
		}

		return builder.build();
	}

	private boolean isNumber(String value) {
		return !value.isEmpty() && Character.isDigit(value.charAt(0));
	}
}

package sw.accounts.pocketmoney;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import sw.accounts.AbstractSummaryReportGenerator;
import sw.accounts.AccountsException;
import sw.accounts.Transaction;
import sw.accounts.io.AbstractTransactionsReader;
import sw.accounts.io.CsvTokeniser;

public class PocketMoneyTransactionsReader extends AbstractTransactionsReader {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "dd/MM/yyyy" );
	
	public void loadTransactions(InputStream aIn) throws AccountsException, IOException {
		final BufferedReader reader = new BufferedReader( new InputStreamReader(aIn) );

		// skip headers

        reader.readLine();

		// read transactions
		
		Transaction previous = null;
		boolean inSplit = false;
		int lineCount = 1;

		String s;
		while ( (s = reader.readLine()) != null ) {
			lineCount++;
			
			try {
				final Transaction t = this.addTransaction( s );
				
				if ( previous != null ) {
					if ( (t != null) && previous.isSplitTransaction(t) ) {
						inSplit = true;
					}
					else if ( inSplit ) {
						this.insertSplitTransactionParent( previous );
						inSplit = false;
					}
				}

				if ( t != null ) {
					previous = t;
				}
			}
			catch (Exception e) {
				throw new AccountsException( "Cannot load pocket-money file: Error at line: "+lineCount, e );
			}
		}
		
		if ( inSplit ) {
			this.insertSplitTransactionParent( previous );			
		}
	}
	
	protected Transaction loadTransaction(String aContent) throws Exception {
		final CsvTokeniser tokeniser = new CsvTokeniser( aContent );

		final Transaction t = new Transaction();
		t.setAccount( tokeniser.nextToken("Account") );

		final String tdate = tokeniser.nextToken("Date");
		final String tid = tokeniser.nextToken("ChkNum");

		t.setPayee( tokeniser.nextToken("Payee") );
		t.setCategory( tokeniser.nextToken("Category") );
		t.setClazz( tokeniser.nextToken("Class") );
		t.setMemo( tokeniser.nextToken("Memo") );
		t.setAmount( Float.parseFloat(tokeniser.nextToken("Amount").replaceAll(",", "")) );		
		t.setCleared(!tokeniser.nextToken("Cleared").isEmpty());

		tokeniser.nextToken("CurrencyCode");
		tokeniser.nextToken("ExchangeRate");
		tokeniser.nextToken("Balance");
		
		if ( tokeniser.hasMoreTokens() ) {
			throw new AccountsException("Too many pocket-money transaction fields");				
		}

		t.setDate( DATE_FORMAT.parse(tdate) );			

		if (!tid.isEmpty()) {
			if ( Character.isDigit(tid.charAt(0)) ) {
				t.setCheckNumber( tid );
				t.setType( PocketMoneySummaryReportGenerator.CHEQUE );
			}
			else if ( tid.indexOf('#') < 0 ) {
				t.setType( tid );
			}
			else if ( "".equals(t.getMemo()) ) {
				t.setMemo( tid );
			}
			else {
				t.setMemo( t.getMemo() + " ["+tid+"]" );
			}
		}

		if ( t.getPayee().startsWith("<") && t.getPayee().endsWith(">") ) {
			final int plen = t.getPayee().length();
			final String tother = t.getPayee().substring( 1, plen-1 );
			
			if ( t.getAmount() < 0 ) {
				// transfer source
				t.setPayee( tother );				
			}
			else {
				// transfer target
				t.setPayee( t.getAccount() );
				t.setTransferOther( tother );
			}

			t.setType( PocketMoneySummaryReportGenerator.ELECTRONIC_TRANSFER );			
		}

		return t;
	}

	protected void insertSplitTransactionParent(Transaction aFirstChild) {
		// find first child
		
		int tpos = this.getTransactions().size() - 1;
		for (;  tpos >= 0;  tpos--) {
			if ( this.getTransactions().get(tpos) == aFirstChild ) {
				break;
			}
		}
		
		// add split transaction parent
		
		final Transaction parent = new Transaction();
		parent.setAccount( aFirstChild.getAccount() );
		parent.setDate( aFirstChild.getDate() );
		parent.setPayee( aFirstChild.getPayee() );
		parent.setCategory( AbstractSummaryReportGenerator.SPLIT );
		parent.setCleared( aFirstChild.isCleared() );
		parent.setType( aFirstChild.getType() );
		
		this.getTransactions().add( tpos-1, parent );

		// update split transaction children
		
		for (;  tpos < this.getTransactions().size();  tpos++) {
			Transaction t = this.getTransactions().get(tpos);
			if ( !t.isSplitTransaction(aFirstChild) ) {
				break;
			}
			
			parent.setAmount( parent.getAmount() + t.getAmount() );
			
			t.setPayee( AbstractSummaryReportGenerator.SPLIT );
			t.setType( "" );
		}
		
		aFirstChild.setPayee( AbstractSummaryReportGenerator.SPLIT );
		aFirstChild.setType( "" );
	}

}

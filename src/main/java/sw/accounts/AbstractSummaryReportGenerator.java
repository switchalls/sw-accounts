package sw.accounts;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import sw.accounts.io.AccountsReader;
import sw.accounts.io.AccountsWriter;
import sw.accounts.io.CategorySummaryGenerator;

public abstract class AbstractSummaryReportGenerator {

	public static final String CHEQUE = "Cheque";
	public static final String ELECTRONIC_TRANSFER = "Elec Trsf";
	public static final String SPLIT = "--SPLIT--";
	
	protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "dd/MM/yyyy" );

	private Collection<Account> accounts;
	private List<Transaction> transactions;
	
	public Account getAccount(String aName) throws AccountsException {
		for ( Account a : this.getAccounts() ) {
			if ( a.getId().equals(aName) ) {
				return a;
			}
		}
		throw new AccountsException("Cannot find account: "+aName );
	}
	
	public Collection<Account> getAccounts() throws AccountsException {
		if ( this.accounts == null ) {
			throw new AccountsException( "Accounts not loaded" );
		}
		return this.accounts;
	}

	public void loadAccounts(File aAccountsFile) throws Exception {
		final AccountsReader reader = new AccountsReader();
		reader.loadAccounts( aAccountsFile );

		this.accounts = reader.getAccounts();
	}

	public void persistAccounts(File aAccountsFile) throws Exception {
		final AccountsWriter writer = new AccountsWriter();
		writer.writeFile( aAccountsFile, this.getAccounts() );
	}

	public void updateAccounts() throws AccountsException {
		for ( Account a : this.getAccounts() ) {
			for ( Transaction t : this.listTransactionsByAccount(a.getId()) ) {
				if ( !t.isSplitTransactionChild() ) {
					a.setBalance( a.getBalance() + t.getAmount() );
				}
			}
		}
	}

	public Collection<Transaction> getTransactions() throws AccountsException {
		if ( this.transactions == null ) {
			throw new AccountsException( "Transactions not loaded" );
		}
		return this.transactions;
	}

	public void loadTransactions(File aTransactionsFile) throws Exception {
		final Calendar startDate = this.createReportDate();

		final Calendar endDate = this.createReportDate();
		endDate.set( Calendar.HOUR_OF_DAY, 23 );
		endDate.set( Calendar.MINUTE, 59 );
		endDate.set( Calendar.SECOND, 59 );
		
		// "discover" last day-in-month by subtracting 1 day from
		// first day in next month
		endDate.roll( Calendar.MONTH, 1 );
		endDate.roll( Calendar.DAY_OF_YEAR, -1 );
		
		this.transactions = this.loadTransactions(
			aTransactionsFile, startDate, endDate
		);

		this.setTransactionTypes();
	}

	public abstract List<Transaction> loadTransactions(
			File aTransactionsFile,
			Calendar aStartDate,
			Calendar aEndDate)
	throws	Exception;
	
	public void generateSummaryReport(File aOutFile) throws AccountsException, IOException {
        try (PrintWriter writer = new PrintWriter(aOutFile)) {
            for (Account a : this.getAccounts()) {
                writer.print(a.getId());
                writer.print(",");
                writer.println(a.getBalance());

                final Collection<Transaction> tlist = this.listTransactionsByAccount(a.getId());
                for (Transaction t : tlist) {
                    writer.print(",");
                    writer.print(",");
                    writer.print(DATE_FORMAT.format(t.getDate()));
                    writer.print(",");

                    if (!t.getCheckNumber().isEmpty()) {
                        writer.print(t.getCheckNumber());
                        writer.print(": ");
                    }

                    writer.print(t.getPayee());
                    writer.print(",");
                    writer.print(t.getCategory());
                    writer.print(",");
                    writer.print(t.getAmount());
                    writer.print(",");
                    writer.print(t.getType());
                    writer.print(",");
                    writer.print(t.getTransferOther());
                    writer.print(",");
                    writer.print(t.isCleared() ? "Yes" : "No");
                    writer.print(",\"");
                    writer.print(t.getMemo());
                    writer.println("\"");
                }

                writer.println();
                writer.println();
            }

            writer.println("Summary");

            final List<CategorySummary> summaries = this.listCategorySummaries();
            summaries.sort((aSummary, aOther) -> aSummary.getCategory().compareTo(aOther.getCategory()));

            for (CategorySummary s : summaries) {
                if (!SPLIT.equals(s.getCategory()) && !"".equals(s.getCategory())) {
                    writer.print(",");
                    writer.print(",");
                    writer.print(s.getCategory());
                    writer.print(",");
                    writer.println(s.getTotal());
                }
            }
        }
	}
	
	protected Calendar createReportDate() {
		Calendar c = GregorianCalendar.getInstance();
		c.set( Calendar.DAY_OF_MONTH, 1 );
		c.set( Calendar.HOUR_OF_DAY, 0 );
		c.set( Calendar.MINUTE, 0 );
		c.set( Calendar.SECOND, 0 );
		c.set( Calendar.MILLISECOND, 0 );
		
		c.roll( Calendar.MONTH, -1 );
		
		if ( c.get(Calendar.MONTH) == Calendar.DECEMBER ) {
			c.roll( Calendar.YEAR, -1 );
		}
		
		return c;
	}

	protected List<CategorySummary> listCategorySummaries() throws AccountsException {
		final CategorySummaryGenerator generator = new CategorySummaryGenerator();
		generator.addTransactions( this.getTransactions() );

		return generator.getSummaries();
	}

	protected Collection<Transaction> listTransactionsByAccount(String aAccount) throws AccountsException {
		final Collection<Transaction> tlist = new ArrayList<>();
		for ( Transaction t : this.getTransactions() ) {
			if ( aAccount.equals(t.getAccount()) ) {
				tlist.add( t );
			}
		}
		return tlist;
	}

	protected void setTransactionTypes() throws AccountsException {
		for ( Transaction t : this.getTransactions() ) {
			if ( "".equals(t.getType()) && !t.isSplitTransactionChild() ) {
				final Account a = this.getAccount( t.getAccount() );
				t.setType( a.getDefaultTransactionType() );
			}
		}
	}

}

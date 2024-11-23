package sw.accounts;

import sw.accounts.exceptions.AccountsException;
import sw.accounts.io.AccountsReader;
import sw.accounts.io.AccountsWriter;
import sw.accounts.io.CategorySummaryGenerator;
import sw.accounts.io.TransactionsReader;
import sw.accounts.models.Account;
import sw.accounts.models.CategorySummary;
import sw.accounts.models.Transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class ReportGenerator {

	protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "dd/MM/yyyy" );

	private final AccountsReader accountsReader;
	private final AccountsWriter accountsWriter;
	private final CategorySummaryGenerator categorySummaryGenerator;
	private final TransactionsReader transactionsReader;

	private Collection<Account> accounts;
	private List<Transaction> transactions;

	@Autowired
	public ReportGenerator(
			AccountsReader accountsReader,
			AccountsWriter accountsWriter,
			CategorySummaryGenerator categorySummaryGenerator,
			TransactionsReader transactionsReader
	) {
		this.accountsReader = accountsReader;
		this.accountsWriter = accountsWriter;
		this.categorySummaryGenerator = categorySummaryGenerator;
		this.transactionsReader = transactionsReader;
	}

	public void loadAccounts(File aAccountsFile) throws Exception {
		this.accounts = this.accountsReader.loadAccounts( aAccountsFile );
	}

	public void persistAccounts(File aAccountsFile) throws Exception {
		this.accountsWriter.writeFile( aAccountsFile, this.getAccounts() );
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
		
		this.transactions = transactionsReader.loadTransactions(
				startDate, endDate, aTransactionsFile
		);

		this.setTransactionTypes();
	}
	
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
            summaries.sort(Comparator.comparing(CategorySummary::getCategory));

            for (CategorySummary s : summaries) {
                if (!Transaction.SPLIT.equals(s.getCategory()) && !"".equals(s.getCategory())) {
                    writer.print(",");
                    writer.print(",");
                    writer.print(s.getCategory());
                    writer.print(",");
                    writer.println(s.getTotal());
                }
            }
        }
	}

	private Collection<Account> getAccounts() throws AccountsException {
		if ( this.accounts == null ) {
			throw new AccountsException( "Accounts not loaded" );
		}
		return this.accounts;
	}

	private Account getAccount(String aName) throws AccountsException {
		return this.accounts.stream()
				.filter((a) -> a.getId().equals(aName))
				.findFirst()
				.orElseThrow( () -> new AccountsException("Cannot find account: "+aName ));
	}

	private Collection<Transaction> getTransactions() throws AccountsException {
		if ( this.transactions == null ) {
			throw new AccountsException( "Transactions not loaded" );
		}
		return this.transactions;
	}

	private Calendar createReportDate() {
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

	private List<CategorySummary> listCategorySummaries() throws AccountsException {
		return this.categorySummaryGenerator.addTransactions( this.getTransactions() );
	}

	private Collection<Transaction> listTransactionsByAccount(String aAccount) {
		return this.transactions.stream()
				.filter((t) -> aAccount.equals(t.getAccount()))
				.collect(Collectors.toList());
	}

	private void setTransactionTypes() throws AccountsException {
		for ( Transaction t : this.getTransactions() ) {
			if ( "".equals(t.getType()) && !t.isSplitTransactionChild() ) {
				final Account a = this.getAccount( t.getAccount() );
				t.setType( a.getDefaultTransactionType() );
			}
		}
	}

}

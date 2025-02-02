package sw.accounts;

import sw.accounts.exceptions.AccountsException;
import sw.accounts.io.AccountsReader;
import sw.accounts.io.AccountsWriter;
import sw.accounts.io.SummaryReportWriter;
import sw.accounts.io.TransactionsReader;
import sw.accounts.models.Account;
import sw.accounts.models.CategorySummary;
import sw.accounts.models.Transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class ReportGenerator {

	private final AccountsReader accountsReader;
	private final AccountsWriter accountsWriter;
	private final CategorySummaryGenerator categorySummaryGenerator;
	private final SummaryReportWriter summaryReportWriter;
	private final TransactionsReader transactionsReader;

	private List<Account> accounts;
	private List<Transaction> transactions;

	@Autowired
	public ReportGenerator(
			AccountsReader accountsReader,
			AccountsWriter accountsWriter,
			CategorySummaryGenerator categorySummaryGenerator,
			SummaryReportWriter summaryReportWriter,
			@Qualifier("gmbh-transactions-reader") TransactionsReader transactionsReader
	) {
		this.accountsReader = accountsReader;
		this.accountsWriter = accountsWriter;
		this.categorySummaryGenerator = categorySummaryGenerator;
		this.summaryReportWriter = summaryReportWriter;
		this.transactionsReader = transactionsReader;
	}

	public List<Account> getAccounts() throws AccountsException {
		if ( this.accounts == null ) {
			throw new AccountsException( "Accounts not loaded" );
		}
		return this.accounts;
	}

	public void loadAccounts(Path accountsFile) throws Exception {
		this.accounts = this.accountsReader.loadAccounts( accountsFile );
	}

	public void persistAccounts(Path accountsFile) throws Exception {
		this.accountsWriter.writeFile( accountsFile, this.getAccounts() );
	}

	public void updateAccounts() throws AccountsException {
		for ( Account a : this.getAccounts() ) {
			for ( Transaction t : this.getTransactionsForAccount(a) ) {
				if ( !t.isSplit() ) {
					a.setBalance( a.getBalance() + t.getAmount() );
				}
			}
		}
	}

	public List<Transaction> getTransactions() throws AccountsException {
		if ( this.transactions == null ) {
			throw new AccountsException( "Transactions not loaded" );
		}
		return this.transactions;
	}

	public void loadTransactions(Calendar startDate, Calendar endDate, Path... transactionFiles) throws Exception {
		this.transactions = transactionsReader.loadTransactions(
			startDate, endDate, transactionFiles
		);

		this.setDefaultTransactionTypes();
	}

	public void generateSummaryReport(Path reportFile) throws AccountsException, IOException {
		try (OutputStream out = Files.newOutputStream(reportFile)) {			
			this.summaryReportWriter.writeReport( out, this.getAccounts(), this.getTransactions(), this.getCategorySummaries() );
		}
	}

	private Account getAccount(String aName) throws AccountsException {
		return this.accounts.stream()
				.filter((a) -> a.getId().equals(aName))
				.findFirst()
				.orElseThrow( () -> new AccountsException("Cannot find account: "+aName ));
	}

	private List<CategorySummary> getCategorySummaries() throws AccountsException {
		return this.categorySummaryGenerator.addTransactions( this.getTransactions() );
	}

	private Collection<Transaction> getTransactionsForAccount(Account aAccount) {
		return this.transactions.stream()
				.filter((t) -> aAccount.getId().equals(t.getAccount()))
				.collect(Collectors.toList());
	}

	private void setDefaultTransactionTypes() throws AccountsException {
		for ( Transaction t : this.getTransactions() ) {
			if ( "".equals(t.getType()) && !t.isSplit() ) {
				final Account a = this.getAccount( t.getAccount() );
				t.setType( a.getDefaultTransactionType() );
			}
		}
	}

}

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

	private List<Account> accounts;
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

	public List<Account> getAccounts() throws AccountsException {
		if ( this.accounts == null ) {
			throw new AccountsException( "Accounts not loaded" );
		}
		return this.accounts;
	}

	public List<Transaction> getTransactions() throws AccountsException {
		if ( this.transactions == null ) {
			throw new AccountsException( "Transactions not loaded" );
		}
		return this.transactions;
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

	public void loadTransactions(Calendar startDate, Calendar endDate, File aTransactionsFile) throws Exception {
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

	private Account getAccount(String aName) throws AccountsException {
		return this.accounts.stream()
				.filter((a) -> a.getId().equals(aName))
				.findFirst()
				.orElseThrow( () -> new AccountsException("Cannot find account: "+aName ));
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

package sw.accounts.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sw.accounts.CategorySummaryGenerator;
import sw.accounts.ReportGenerator;
import sw.accounts.io.pocketmoney.PocketMoneyTransactionsReader;
import sw.accounts.models.Account;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class ReportGeneratorTest
{
	private static final Account BARCLAYS_ACCOUNT = Account.builder()
			.id("Barclays")
			.defaultTransactionType("Visa")
			.build();

	private static final Account FIRSTDIRECT_ACCOUNT = Account.builder()
			.id("Firstdirect")
			.defaultTransactionType("")
			.build();

	private static final Account GOLDFISH_ACCOUNT = Account.builder()
			.id("Goldfish")
			.defaultTransactionType("Mastercard")
			.build();

	@Mock
	private AccountsWriter mockAccountsWriter;

	@Mock
	private SummaryReportWriter mockSummaryReportWriter;

	private ReportGenerator testSubject;

	@BeforeEach
	public void setupTestSubject() {
		final AccountsReader accountsReader = new AccountsReader();

		final CategorySummaryGenerator categorySummaryGenerator = new CategorySummaryGenerator();

		final TransactionsReader transactionsReader = new PocketMoneyTransactionsReader();

		testSubject = new ReportGenerator(accountsReader, mockAccountsWriter, categorySummaryGenerator, mockSummaryReportWriter, transactionsReader );
	}

	@Test
	void shouldUpdateAccountsForSeptember() throws Exception {
		testSubject.loadAccounts( aResource("AccountTotals_2024_09.csv") );
		testSubject.loadTransactions( aFirstDayOf(9), aLastDayOf(9), aResource("Transactions_2024_09.csv") );
		testSubject.updateAccounts();

		final List<Account> accounts = testSubject.getAccounts();
		assertEquals( aBalanceOf(BARCLAYS_ACCOUNT, 1480.8802F), accounts.get(0) );
		assertEquals( aBalanceOf(FIRSTDIRECT_ACCOUNT, 4460.4F), accounts.get(1) );
		assertEquals( aBalanceOf(GOLDFISH_ACCOUNT, -15.980183F), accounts.get(2) );
	}

	@Test
	void shouldUpdateAccountsForOctober() throws Exception {
		testSubject.loadAccounts( aResource("AccountTotals_2024_10.csv") );
		testSubject.loadTransactions( aFirstDayOf(10), aLastDayOf(10), aResource("Transactions_2024_10.csv") );
		testSubject.updateAccounts();

		final List<Account> accounts = testSubject.getAccounts();
		assertEquals( aBalanceOf(BARCLAYS_ACCOUNT, 1514.2194F), accounts.get(0) );
		assertEquals( aBalanceOf(FIRSTDIRECT_ACCOUNT, 4633.0703F), accounts.get(1) );
		assertEquals( aBalanceOf(GOLDFISH_ACCOUNT, 23.71464F), accounts.get(2) );
	}

	private Account aBalanceOf(Account account, float expected) {
		return account.toBuilder()
				.balance(expected)
				.build();
	}

	private Calendar aFirstDayOf(int month) {
		final Calendar startDate = GregorianCalendar.getInstance();
		startDate.set( Calendar.DAY_OF_MONTH, 1 );
		startDate.set( Calendar.HOUR_OF_DAY, 0 );
		startDate.set( Calendar.MINUTE, 0 );
		startDate.set( Calendar.SECOND, 0 );
		startDate.set( Calendar.MILLISECOND, 0 );

		startDate.set( Calendar.MONTH, month - 1 );
		startDate.set( Calendar.YEAR, 2024 );

		return startDate;
	}

	private Calendar aLastDayOf(int month) {
		final Calendar endDate = this.aFirstDayOf(month);
		endDate.set( Calendar.HOUR_OF_DAY, 23 );
		endDate.set( Calendar.MINUTE, 59 );
		endDate.set( Calendar.SECOND, 59 );

		// "discover" last day-in-month by subtracting 1 day from
		// first day in next month
		endDate.roll( Calendar.MONTH, 1 );
		endDate.roll( Calendar.DAY_OF_YEAR, -1 );

		return endDate;
	}

	private File aResource(String name) throws URISyntaxException {
		final URL resourceUrl = this.getClass().getResource(name );
		assertNotNull( resourceUrl );

		return Paths.get(resourceUrl.toURI()).toFile();
	}
}

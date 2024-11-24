package sw.accounts.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
	private static final Account EXPECTED_BARCLAYS_FOR_SEPTEMBER = Account.builder()
			.balance(1480.8802F)
			.id("Barclays")
			.defaultTransactionType("Visa")
			.build();

	private static final Account EXPECTED_FIRSTDIRECT_FOR_SEPTEMBER = Account.builder()
			.balance(4460.4F)
			.id("Firstdirect")
			.defaultTransactionType("")
			.build();

	private static final Account EXPECTED_GOLDFISH_FOR_SEPTEMBER = Account.builder()
			.balance(-15.980183F)
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
	void shouldUpdateAccounts() throws Exception {
		testSubject.loadAccounts( aResource("AccountTotals_2024_08.csv") );
		testSubject.loadTransactions( aDateForFirstOfSeptember(), aDateForEndOfSeptember(), aResource("Transactions_2024_09.csv") );
		testSubject.updateAccounts();

		final List<Account> accounts = testSubject.getAccounts();
		assertEquals(EXPECTED_BARCLAYS_FOR_SEPTEMBER, accounts.get(0) );
		assertEquals(EXPECTED_FIRSTDIRECT_FOR_SEPTEMBER, accounts.get(1) );
		assertEquals(EXPECTED_GOLDFISH_FOR_SEPTEMBER, accounts.get(2) );
	}

	private Calendar aDateForFirstOfSeptember() {
		final Calendar startDate = GregorianCalendar.getInstance();
		startDate.set( Calendar.DAY_OF_MONTH, 1 );
		startDate.set( Calendar.HOUR_OF_DAY, 0 );
		startDate.set( Calendar.MINUTE, 0 );
		startDate.set( Calendar.SECOND, 0 );
		startDate.set( Calendar.MILLISECOND, 0 );

		startDate.set( Calendar.MONTH, 8 );
		startDate.set( Calendar.YEAR, 2024 );

		return startDate;
	}

	private Calendar aDateForEndOfSeptember() {
		final Calendar endDate = this.aDateForFirstOfSeptember();
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

package sw.accounts.io.pocketmoney;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sw.accounts.models.Transaction;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class PocketMoneyTransactionsReaderTest
{
	private static final Transaction CLEARED_ELECTRONIC_TRANSFER = Transaction.builder()
			.type("Elec Trsf")
			.category("")
			.clazz("")
			.cleared(true)
			.date( aDateFor(2024, 9, 1) )
			.memo("")
			.build();

	private static final Transaction EXPECTED_TO_CASHBACK = CLEARED_ELECTRONIC_TRANSFER.toBuilder()
			.account("Firstdirect")
			.amount(-750F)
			.payee("CashBack Card")
			.transferOther(null)
			.build();

	private static final Transaction EXPECTED_FROM_FIRSTDIRECT = CLEARED_ELECTRONIC_TRANSFER.toBuilder()
			.account("CashBack Card")
			.amount(750F)
			.payee("CashBack Card")
			.transferOther("Firstdirect")
			.build();

	@InjectMocks
	private PocketMoneyTransactionsReader testSubject;

	@Test
	void shouldCreateSplitTransactions() throws Exception {
		final List<Transaction> transactions = testSubject.loadTransactions( aDateForFirstOfSeptember(), aDateForEndOfSeptember(), aResource("SplitTransactions_2024_09.csv") );
		for (Transaction t : transactions) {
			System.out.println("splits: " + t);
		}
	}

	@Test
	void shouldCreateTransfers() throws Exception {
		final List<Transaction> transactions = testSubject.loadTransactions( aDateForFirstOfSeptember(), aDateForEndOfSeptember(), aResource("Transfers_2024_09.csv") );
		assertEquals( EXPECTED_TO_CASHBACK, transactions.get(0) );
		assertEquals( EXPECTED_FROM_FIRSTDIRECT, transactions.get(1) );
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

	private static Date aDateFor(int year, int month, int dayOfMonth) {
		return Date.from(
				LocalDate.of(year, month, dayOfMonth )
					.atStartOfDay()
					.atZone( ZoneId.systemDefault() )
					.toInstant() );
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

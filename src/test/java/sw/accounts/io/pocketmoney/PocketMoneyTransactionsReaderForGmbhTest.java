package sw.accounts.io.pocketmoney;

import static org.junit.jupiter.api.Assertions.*;

import sw.accounts.io.pocketmoney.csv.GmbhCsvReader;
import sw.accounts.models.Transaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class PocketMoneyTransactionsReaderForGmbhTest
{
	private static final Transaction ELECTRONIC_TRANSFER = Transaction.builder()
			.type("Elec Trsf")
			.category("")
			.cleared(false)
			.date( aDateFor(2024, 11, 25) )
			.memo("")
			.build();

	private static final Transaction EXPECTED_TO_FIRSTDIRECT = ELECTRONIC_TRANSFER.toBuilder()
			.account("Barclays")
			.amount(-650F)
			.payee("Transfer to Firstdirect")
			.transferOther("Firstdirect")
			.build();

	private static final Transaction EXPECTED_FROM_BARCLAYS = ELECTRONIC_TRANSFER.toBuilder()
			.account("Firstdirect")
			.amount(650F)
			.payee("Transfer from Barclays")
			.transferOther("Barclays")
			.build();

	private static final Transaction BARCLAYS_SPLIT_PARENT = Transaction.builder()
			.account("Barclays")
			.type("Direct Debit")
			.category(Transaction.SPLIT)
			.cleared(true)
			.memo("")
			.build();

	private final PocketMoneyTransactionsReader testSubject = new PocketMoneyTransactionsReader( new GmbhCsvReader() );

	@Test
	void shouldCreateSplitTransactions() throws Exception {
		final List<Transaction> transactions = testSubject.loadTransactions( aFirstDayOf(10), aLastDayOf(10), aResource("Gmbh_SplitTransactions_2024_10.csv") );

		assertEquals( BARCLAYS_SPLIT_PARENT.toBuilder()
				.amount(-31.97F)
				.payee("ID Mobile")
				.date( aDateFor(2024, 10, 30) )
				.build(), transactions.get(0) );

		assertEquals( aPhoneBillSplitOf(-3.99F, "Children:Phone", "Olen: Paid 30/09"), transactions.get(1) );
		assertEquals( aPhoneBillSplitOf(-6F, "Children:Phone", "Eirwen"), transactions.get(2) );
		assertEquals( aPhoneBillSplitOf(-17.99F, "Personal:Phone", ""), transactions.get(3) );
		assertEquals( aPhoneBillSplitOf(-3.99F, "Children:Phone", "Olen"), transactions.get(4) );

		assertEquals( BARCLAYS_SPLIT_PARENT.toBuilder()
				.amount(-274.36F)
				.payee("Utility Warehouse")
				.cleared(false)
				.date( aDateFor(2024, 10, 31) )
				.build(), transactions.get(5) );

		assertEquals( aHouseBillSplitOf(-105.86F, "House:Bills", "Gas"), transactions.get(6) );
		assertEquals( aHouseBillSplitOf(-121.95F, "House:Bills", "Electric"), transactions.get(7) );
		assertEquals( aHouseBillSplitOf(-53.97F, "House:Bills", "Phone, broadband etc"), transactions.get(8) );
		assertEquals( aHouseBillSplitOf(-27.19F, "House:Bills", "House insurance"), transactions.get(9) );
		assertEquals( aHouseBillSplitOf(34.61F, "Refund", ""), transactions.get(10) );

		final Transaction lastTransaction = transactions.get(11);
		assertNotEquals( Transaction.SPLIT, lastTransaction.getCategory() );
		assertNotEquals( Transaction.SPLIT, lastTransaction.getPayee() );
	}

	@Test
	void shouldCreateTransfers() throws Exception {
		final List<Transaction> transactions = testSubject.loadTransactions( aFirstDayOf(11), aLastDayOf(11), aResource("Gmbh_Transfers_2024_10.csv") );
		assertEquals(EXPECTED_TO_FIRSTDIRECT, transactions.get(0) );
		assertEquals(EXPECTED_FROM_BARCLAYS, transactions.get(1) );
	}

	private static Date aDateFor(int year, int month, int dayOfMonth) {
		return Date.from(
				LocalDate.of(year, month, dayOfMonth )
						.atStartOfDay()
						.atZone( ZoneId.systemDefault() )
						.toInstant() );
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

	private Path aResource(String name) throws URISyntaxException {
		final URL resourceUrl = this.getClass().getResource(name );
		assertNotNull( resourceUrl );

		return Paths.get(resourceUrl.toURI());
	}

	private Transaction aHouseBillSplitOf(float amount, String category, String memo) {
		return Transaction.builder()
				.account("Barclays")
				.amount(amount)
				.payee(Transaction.SPLIT)
				.type("")
				.category(category)
				.clazz("")
				.cleared(false)
				.date( aDateFor(2024, 10, 31) )
				.memo(memo)
				.build();
	}

	private Transaction aPhoneBillSplitOf(float amount, String category, String memo) {
		return Transaction.builder()
				.account("Barclays")
				.amount(amount)
				.payee(Transaction.SPLIT)
				.type("")
				.category(category)
				.clazz("")
				.cleared(true)
				.date( aDateFor(2024, 10, 30) )
				.memo(memo)
				.build();
	}
}

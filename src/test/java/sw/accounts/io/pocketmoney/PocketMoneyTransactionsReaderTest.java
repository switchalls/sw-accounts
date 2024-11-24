package sw.accounts.io.pocketmoney;

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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PocketMoneyTransactionsReaderTest
{
	private static final Transaction CLEARED_ELECTRONIC_TRANSFER = Transaction.builder()
			.clazz("")
			.type("Elec Trsf")
			.category("")
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

	private static final Transaction BARCLAYS_SPLIT_PARENT = Transaction.builder()
			.account("Barclays")
			.type("Direct Debit")
			.clazz("Direct Debit")
			.category("--SPLIT--")
			.cleared(true)
			.memo("")
			.build();

	@InjectMocks
	private PocketMoneyTransactionsReader testSubject;

	@Test
	void shouldCreateSplitTransactions() throws Exception {
		final List<Transaction> transactions = testSubject.loadTransactions( aDateForFirstOfSeptember(), aDateForEndOfSeptember(), aResource("SplitTransactions_2024_09.csv") );

		assertEquals( BARCLAYS_SPLIT_PARENT.toBuilder()
				.amount(-25.039999F)
				.payee("ID Mobile")
				.date( aDateFor(2024, 9, 24) )
				.build(), transactions.get(0) );

		assertEquals( aPhoneBillSplitOf(-6F, "Children:Phone", "Eirwen"), transactions.get(1) );
		assertEquals( aPhoneBillSplitOf(-17.99F, "Children:Phone", "Olen"), transactions.get(2) );
		assertEquals( aPhoneBillSplitOf(-1.05F, "Personal:Phone", "Final payment"), transactions.get(3) );

		assertEquals( BARCLAYS_SPLIT_PARENT.toBuilder()
				.amount(-287.54F)
				.payee("Utility Warehouse")
				.cleared(false)
				.date( aDateFor(2024, 9, 30) )
				.build(), transactions.get(4) );

		assertEquals( aHouseBillSplitOf(-105.86F, "House:Bills", "Gas"), transactions.get(5) );
		assertEquals( aHouseBillSplitOf(-121.95F, "House:Bills", "Electric"), transactions.get(6) );
		assertEquals( aHouseBillSplitOf(-53.97F, "House:Bills", "Phone, broadband etc"), transactions.get(7) );
		assertEquals( aHouseBillSplitOf(-27.19F, "House:Bills", "House insurance"), transactions.get(8) );
		assertEquals( aHouseBillSplitOf(21.43F, "Refund", ""), transactions.get(9) );

		assertNotEquals( Transaction.SPLIT, transactions.get(10).getCategory() );
		assertNotEquals( Transaction.SPLIT, transactions.get(10).getPayee() );
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

	private Transaction aHouseBillSplitOf(float amount, String category, String memo) {
		return Transaction.builder()
				.account("Barclays")
				.amount(amount)
				.payee(Transaction.SPLIT)
				.type("")
				.category(category)
				.clazz("")
				.cleared(false)
				.date( aDateFor(2024, 9, 30) )
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
				.date( aDateFor(2024, 9, 24) )
				.memo(memo)
				.build();
	}
}
package sw.accounts.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import sw.accounts.io.pocketmoney.csv.GmbhCsvReader;
import sw.accounts.io.pocketmoney.PocketMoneyTransactionsReader;
import sw.accounts.models.Account;
import sw.accounts.models.CategorySummary;
import sw.accounts.models.Transaction;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class SummaryReportWriterTest {
	
	private static final Account BARCLAYS_ACCOUNT = Account.builder()
		.id("Barclays")
		.build();
	
	private static final String EXPECTED_BARCLAYS_REPORT = """
Account:,Barclays,,20/10/2024,M. Howard,Gift,100.00,,,Yes,
End Balance:,0.00,,21/10/2024,Datalex,Salary,5034.04,,,Yes,
,,,22/10/2024,Vodaphone,Personal:Phone,-25.00,,,Yes,
,,,23/10/2024,Utility Warehouse,--SPLIT--,-263.06,Direct Debit,,No,
,,,23/10/2024,--SPLIT--,House:Bills,-105.86,,,,"Gas"
,,,23/10/2024,--SPLIT--,House:Bills,-121.95,,,,"Electric"
,,,23/10/2024,--SPLIT--,House:Bills,-45.94,,,,"Phone, broadband etc"
,,,23/10/2024,--SPLIT--,House:Bills,-32.69,,,,"House insurance"
,,,23/10/2024,--SPLIT--,Refund,43.38,,,,


Summary
,,Total Income,0.00
,,Total Expenses,0.00
""";

	private static final String EXPECTED_CATEGORY_SUMMARY_REPORT = """
Summary
,,House,-75.00
,,House:Bills,-50.00
,,House:Repair,-25.00
,,Salary,99.00
,,Salary:Personal,66.00
,,Salary:Sharon,33.00
,,Total Income,99.00
,,Total Expenses,-75.00
""";

	@InjectMocks
	private SummaryReportWriter testSubject;

	@Test
	void shouldWriteAccountReport() throws Exception {				
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		testSubject.writeReport(
			out,
			Arrays.asList( BARCLAYS_ACCOUNT ),
			aListOfTransactionsForBarclays(),
			new ArrayList<>()
		);
		
		assertEquals( EXPECTED_BARCLAYS_REPORT, out.toString());
	}

	@Test
	void shouldWriteCategorySummaryReport() throws Exception {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		testSubject.writeReport(
			out,
			Collections.emptyList(),
			Collections.emptyList(),
			aListOfCategorySummary()
		);
		
		assertEquals( EXPECTED_CATEGORY_SUMMARY_REPORT, out.toString());
	}
	
	private List<CategorySummary> aListOfCategorySummary() {
		final List<CategorySummary> summaries = new ArrayList<>();
		
		// invalid category
		summaries.add( CategorySummary.builder()
			.category( "" )
			.total( 101 )
			.build() );

		// TODO - When does category = split ??
		summaries.add( CategorySummary.builder()
			.category( Transaction.SPLIT )
			.total( 102 )
			.build() );
			
		summaries.add( CategorySummary.builder()
			.category( "Salary" )
			.total( 99 )
			.build() );

		summaries.add( CategorySummary.builder()
			.category( "Salary:Personal" )
			.total( 66 )
			.build() );

		summaries.add( CategorySummary.builder()
			.category( "Salary:Sharon" )
			.total( 33 )
			.build() );

		summaries.add( CategorySummary.builder()
			.category( "House" )
			.total( -75 )
			.build() );
			
		summaries.add( CategorySummary.builder()
			.category( "House:Bills" )
			.total( -50 )
			.build() );

		summaries.add( CategorySummary.builder()
			.category( "House:Repair" )
			.total( -25 )
			.build() );
			
		return summaries;
	}
	
	private List<Transaction> aListOfTransactionsForBarclays() throws Exception {
		final PocketMoneyTransactionsReader transactionsReader = new PocketMoneyTransactionsReader( new GmbhCsvReader() );
		final List<Transaction> transactions = transactionsReader.loadTransactions(
			aFirstDayOf(10), aLastDayOf(10), aResource("Gmbh_Transactions_SummaryReportWriter.csv")
		);
		
		transactions.forEach((t) -> System.err.println("loaded transactions: " + t));

		return transactions;
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
}
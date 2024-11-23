package sw.accounts;

import java.io.File;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConsoleApplication implements CommandLineRunner {

    @Autowired
    private ReportGenerator reportGenerator;

    public static void main(String[] args) {
        SpringApplication.run( ConsoleApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if ( args.length != 3 ) {
            printUsage(System.err);
            return;
        }

        final Calendar startDate = this.createReportStartDate();
        final Calendar endDate = this.createReportEndDate();

        reportGenerator.loadAccounts( new File(args[0]) );
        reportGenerator.loadTransactions( startDate, endDate, new File(args[1]) );
        reportGenerator.updateAccounts();
        reportGenerator.generateSummaryReport( new File(args[2]) );
        reportGenerator.persistAccounts( new File(args[0]) );
    }

    private void printUsage(PrintStream out) {
        out.println("Usage: ReportGenerator <accounts.csv> <transactions.csv> <output.csv>");
    }

    private Calendar createReportStartDate() {
        final Calendar startDate = GregorianCalendar.getInstance();
        startDate.set( Calendar.DAY_OF_MONTH, 1 );
        startDate.set( Calendar.HOUR_OF_DAY, 0 );
        startDate.set( Calendar.MINUTE, 0 );
        startDate.set( Calendar.SECOND, 0 );
        startDate.set( Calendar.MILLISECOND, 0 );

        startDate.roll( Calendar.MONTH, -1 );

        if ( startDate.get(Calendar.MONTH) == Calendar.DECEMBER ) {
            startDate.roll( Calendar.YEAR, -1 );
        }

        return startDate;
    }

    private Calendar createReportEndDate() {
        final Calendar endDate = this.createReportStartDate();
        endDate.set( Calendar.HOUR_OF_DAY, 23 );
        endDate.set( Calendar.MINUTE, 59 );
        endDate.set( Calendar.SECOND, 59 );

        // "discover" last day-in-month by subtracting 1 day from
        // first day in next month
        endDate.roll( Calendar.MONTH, 1 );
        endDate.roll( Calendar.DAY_OF_YEAR, -1 );

        return endDate;
    }
}
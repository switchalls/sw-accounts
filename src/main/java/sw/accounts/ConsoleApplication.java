package sw.accounts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
        final Path accountsFile = Paths.get(args[0]);
        final Path transactionFiles  = Paths.get(args[1]);
        final Path reportFile  = Paths.get(args[2]);

        reportGenerator.loadAccounts( accountsFile );
        reportGenerator.loadTransactions( startDate, endDate, listCsvFiles(transactionFiles) );
        reportGenerator.updateAccounts();
        reportGenerator.generateSummaryReport( reportFile );
        reportGenerator.persistAccounts( accountsFile );
    }

    private void printUsage(PrintStream out) {
        out.println("Usage: ReportGenerator <accounts.csv> <transactions folder> <output.csv>");
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

    private Path[] listCsvFiles(Path rootFile) throws IOException {
        if (rootFile.toFile().isDirectory()) {
            return Files.walk(rootFile)
                    .filter((p) -> p.endsWith(".csv"))
                    .toArray(Path[]::new);
        }

        return new Path[] { rootFile };
    }
}
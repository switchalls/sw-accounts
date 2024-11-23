package sw.accounts;

import java.io.File;
import java.io.PrintStream;

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

        reportGenerator.loadAccounts( new File(args[0]) );
        reportGenerator.loadTransactions( new File(args[1]) );
        reportGenerator.updateAccounts();
        reportGenerator.generateSummaryReport( new File(args[2]) );
        reportGenerator.persistAccounts( new File(args[0]) );
    }

    private void printUsage(PrintStream out) {
        out.println("Usage: ReportGenerator <accounts.csv> <transactions.csv> <output.csv>");
    }
}
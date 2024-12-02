package sw.accounts.io;

import sw.accounts.models.Account;
import sw.accounts.models.CategorySummary;
import sw.accounts.models.Transaction;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SummaryReportWriter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "dd/MM/yyyy" );

    public void writeReport(
            Path reportFile,
            List<Account> accounts,
            List<Transaction> transactions,
            List<CategorySummary> categorySummaries)
    throws  IOException {

        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(reportFile))) {
            for (Account a : accounts) {
                int lineCount = 0;

                final Collection<Transaction> tlist = this.getTransactionsForAccount(a, transactions);
                for (Transaction t : tlist) {
                    if ( lineCount < 1 ) {
                        this.printAccountId( writer, a );
                    } else if ( lineCount < 2 ) {
                        this.printAccountBalance( writer, a );
                    } else {
                        writer.print(",,,");
                    }

                    this.printDate( writer, t.getDate() );
                    writer.print(",");

                    if (StringUtils.hasLength(t.getCheckNumber())) {
                        writer.print( t.getCheckNumber() );
                        writer.print(": ");
                    }

                    writer.print( t.getPayee() );
                    writer.print(",");
                    writer.print( t.getCategory() );
                    writer.print(",");
                    this.printFloat( writer, t.getAmount() );
                    writer.print(",");
                    this.printString( writer, t.getType() );
                    writer.print(",");
                    this.printString( writer, t.getTransferOther() );
                    writer.print(",");

                    if (!t.isSplit()) {
                        writer.print(t.isCleared() ? "Yes" : "No");
                    }

                    writer.print(",");

                    if (StringUtils.hasLength(t.getMemo())) {
                        writer.print( "\"" + t.getMemo() + "\"" );
                    }

                    writer.println();
                    lineCount++;
                }

                switch ( lineCount ) {
                    case 0:
                        this.printAccountId( writer, a );
                        writer.println(",,,,,,,");

                        // drop through

                    case 1:
                        this.printAccountBalance( writer, a );
                        writer.println(",,,,,,,");
                }

                writer.println();
                writer.println();
            }

            writer.println("Summary");

            categorySummaries.sort(Comparator.comparing(CategorySummary::getCategory));

            for (CategorySummary s : categorySummaries) {
                if (StringUtils.hasLength(s.getCategory()) && !Transaction.SPLIT.equals(s.getCategory())) {
                    writer.print(",");
                    writer.print(",");
                    writer.print( s.getCategory() );
                    writer.print(",");
                    this.printFloat( writer, s.getTotal() );
                    writer.println();
                }
            }
        }
    }

    private Collection<Transaction> getTransactionsForAccount(Account aAccount, List<Transaction> transactions) {
        return transactions.stream()
                .filter((t) -> aAccount.getId().equals(t.getAccount()))
                .collect(Collectors.toList());
    }

    private void printAccountId(PrintWriter writer, Account account) {
        writer.print("Account:,");
        writer.print( account.getId() );
        writer.print(",,");
    }

    private void printAccountBalance(PrintWriter writer, Account account) {
        writer.print("End Balance:,");
        printFloat( writer, account.getBalance() );
        writer.print(",,");
    }

    private void printDate(PrintWriter writer, Date value) {
        writer.print( DATE_FORMAT.format(value) );
    }

    private void printFloat(PrintWriter writer, float value) {
        writer.print( String.format("%.2f", value) );
    }

    private void printString(PrintWriter writer, String value) {
        if (StringUtils.hasLength(value)) {
            writer.print(value);
        }
    }
}

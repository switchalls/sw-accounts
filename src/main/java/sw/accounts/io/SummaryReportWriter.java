package sw.accounts.io;

import sw.accounts.models.Account;
import sw.accounts.models.CategorySummary;
import sw.accounts.models.Transaction;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SummaryReportWriter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "dd/MM/yyyy" );

    public void writeReport(
            File aOutFile,
            List<Account> accounts,
            List<Transaction> transactions,
            List<CategorySummary> categorySummaries)
    throws  IOException {

        try (PrintWriter writer = new PrintWriter(aOutFile)) {
            for (Account a : accounts) {
                writer.print(a.getId());
                writer.print(",");
                writer.println(a.getBalance());

                final Collection<Transaction> tlist = this.getTransactionsForAccount(a, transactions);
                for (Transaction t : tlist) {
                    writer.print(",");
                    writer.print(",");
                    writer.print(DATE_FORMAT.format(t.getDate()));
                    writer.print(",");

                    if (!t.getCheckNumber().isEmpty()) {
                        writer.print(t.getCheckNumber());
                        writer.print(": ");
                    }

                    writer.print(t.getPayee());
                    writer.print(",");
                    writer.print(t.getCategory());
                    writer.print(",");
                    writer.print( String.format("%.2f", t.getAmount()) );
                    writer.print(",");
                    writer.print(t.getType());
                    writer.print(",");
                    writer.print(t.getTransferOther());
                    writer.print(",");
                    writer.print(t.isCleared() ? "Yes" : "No");
                    writer.print(",\"");
                    writer.print(t.getMemo());
                    writer.println("\"");
                }

                writer.println();
                writer.println();
            }

            writer.println("Summary");

            categorySummaries.sort(Comparator.comparing(CategorySummary::getCategory));

            for (CategorySummary s : categorySummaries) {
                if (!Transaction.SPLIT.equals(s.getCategory()) && !"".equals(s.getCategory())) {
                    writer.print(",");
                    writer.print(",");
                    writer.print(s.getCategory());
                    writer.print(",");
                    writer.println(s.getTotal());
                }
            }
        }
    }

    private Collection<Transaction> getTransactionsForAccount(Account aAccount, List<Transaction> transactions) {
        return transactions.stream()
                .filter((t) -> aAccount.getId().equals(t.getAccount()))
                .collect(Collectors.toList());
    }
}

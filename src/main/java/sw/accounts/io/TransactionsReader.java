package sw.accounts.io;

import sw.accounts.models.Transaction;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.List;

public interface TransactionsReader {

    List<Transaction> loadTransactions(Calendar startDate, Calendar endDate, Path... transactionFiles) throws Exception;

}


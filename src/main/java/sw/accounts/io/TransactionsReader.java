package sw.accounts.io;

import sw.accounts.models.Transaction;

import java.io.File;
import java.util.Calendar;
import java.util.List;

public interface TransactionsReader {

    List<Transaction> loadTransactions(Calendar startDate, Calendar endDate, File aPath) throws Exception;

}


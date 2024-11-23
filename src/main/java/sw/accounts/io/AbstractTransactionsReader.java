package sw.accounts.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import sw.accounts.AccountsException;
import sw.accounts.Transaction;

public abstract class AbstractTransactionsReader {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	private Calendar endDate;
	private Calendar startDate;
	private final List<Transaction> transactions = new ArrayList<>();

	public List<Transaction> getTransactions() {
		return this.transactions;
	}

	public void setDateRange(Calendar aStart, Calendar aEnd) {
		this.startDate = aStart;
		this.endDate = aEnd;
	}

	public void loadTransactions(File aPath) throws Exception {
		try (InputStream in = Files.newInputStream(aPath.toPath())) {
			this.loadTransactions(in);
		}
	}

	protected Transaction addTransaction(String aContent) throws Exception {
		final Transaction t = this.loadTransaction(aContent);

		final Calendar c = GregorianCalendar.getInstance();
		c.setTime(t.getDate());

		if (c.before(this.startDate)) {
			System.out.println("Ignoring transaction before " + DATE_FORMAT.format(this.startDate.getTime()) + ": " + t);
			return null;
		}

		if ((this.endDate != null) && this.endDate.before(c)) {
			System.out.println("Ignoring transaction after " + DATE_FORMAT.format(this.endDate.getTime()) + ": " + t);
			return null;
		}

		this.transactions.add(t);

		return t;
	}

	protected abstract Transaction loadTransaction(String aContent) throws Exception;

	public abstract void loadTransactions(InputStream aIn) throws AccountsException, IOException;

}

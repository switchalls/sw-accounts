package sw.accounts.io;

import sw.accounts.models.Account;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

@Component
public class AccountsWriter
{
	public void writeFile(Path path, Collection<Account> aAccounts) throws IOException {
		this.writeFile(Files.newOutputStream(path), aAccounts );
	}

	private void writeFile(OutputStream out, Collection<Account> aAccounts) {
		final PrintWriter writer = new PrintWriter( out );
		for ( Account a : aAccounts ) {
			writer.print( a.getId() );
			writer.print( "," );
			this.printFloat( writer, a.getBalance() );
			writer.print( "," );
			writer.println( a.getDefaultTransactionType() );
		}
		
		writer.close();
	}

	private void printFloat(PrintWriter writer, float amount) {
		writer.print( String.format("%.2f", amount) );
	}
}

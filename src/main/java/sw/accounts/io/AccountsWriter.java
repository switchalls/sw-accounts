package sw.accounts.io;

import sw.accounts.models.Account;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Collection;

@Component
public class AccountsWriter
{
	public void writeFile(File aPath, Collection<Account> aAccounts) throws IOException {
		this.writeFile(Files.newOutputStream(aPath.toPath()), aAccounts );
	}

	private void writeFile(OutputStream aOut, Collection<Account> aAccounts) {
		final PrintWriter writer = new PrintWriter( aOut );
		for ( Account a : aAccounts ) {
			writer.print( a.getId() );
			writer.print( "," );
			writer.print( String.format("%.2f", a.getBalance()) );
			writer.print( "," );
			writer.println( a.getDefaultTransactionType() );
		}
		
		writer.close();
	}

}

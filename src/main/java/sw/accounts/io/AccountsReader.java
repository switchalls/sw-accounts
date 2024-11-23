package sw.accounts.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import sw.accounts.Account;
import sw.accounts.AccountsException;

public class AccountsReader {

	private final List<Account> accounts = new ArrayList<>();
	
	public List<Account> getAccounts()
	{
		return this.accounts;
	}
	
	public void loadAccounts(File aPath) throws AccountsException, IOException {
        try (InputStream in = Files.newInputStream(aPath.toPath())) {
            this.loadAccounts(in);
        }
	}

	public void loadAccounts(InputStream aIn)
	throws AccountsException, IOException
	{
		final BufferedReader reader = new BufferedReader( new InputStreamReader(aIn) );

		String s;
		while ( (s = reader.readLine()) != null ) {
			final CsvTokeniser tokeniser = new CsvTokeniser( s );
			final String account = tokeniser.nextToken("Account");			
			final String balance = tokeniser.nextToken("Balance");
			final String defaultType = tokeniser.nextToken("DefaultTransactionType");
			
			if ( tokeniser.hasMoreTokens() )
			{
				throw new AccountsException("Invalid accounts file: Too many fields");				
			}
			
			try {
				final Account a = new Account();
				a.setId( account );
				a.setBalance( Float.parseFloat(balance) );
				a.setDefaultTransactionType( defaultType );

				this.accounts.add( a );
			}
			catch (NumberFormatException e)
			{
				throw new AccountsException("Invalid accounts file: Bad balance "+balance);
			}
		}
		
	}

}

package sw.accounts.io;

import org.springframework.stereotype.Component;
import sw.accounts.exceptions.AccountsException;
import sw.accounts.io.csv.CsvTokeniser;
import sw.accounts.models.Account;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
public class AccountsReader {

    public List<Account> loadAccounts(File aPath) throws AccountsException, IOException {
        try (InputStream in = Files.newInputStream(aPath.toPath())) {
            return this.loadAccounts(in);
        }
	}

	private List<Account> loadAccounts(InputStream aIn) throws AccountsException, IOException {
		final List<Account> newAccounts = new ArrayList<>();

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
				final Account a = Account.builder()
						.id( account )
						.balance( Float.parseFloat(balance) )
						.defaultTransactionType( defaultType )
						.build();

				newAccounts.add( a );
			}
			catch (NumberFormatException e)
			{
				throw new AccountsException("Invalid accounts file: Bad balance "+balance);
			}
		}

		return newAccounts;
	}

}

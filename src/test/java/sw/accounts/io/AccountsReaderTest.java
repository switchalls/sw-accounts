package sw.accounts.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sw.accounts.models.Account;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AccountsReaderTest
{
	private static final Account BARCLAYS_ACCOUNT = Account.builder()
			.id("Barclays")
			.defaultTransactionType("Visa")
			.build();

	private static final Account FIRSTDIRECT_ACCOUNT = Account.builder()
			.id("Firstdirect")
			.defaultTransactionType("")
			.build();

	private static final Account GOLDFISH_ACCOUNT = Account.builder()
			.id("Goldfish")
			.defaultTransactionType("Mastercard")
			.build();

	@InjectMocks
	private AccountsReader testSubject;

	@Test
	void shouldLoadAccountTotals() throws Exception {
		final List<Account> accounts = testSubject.loadAccounts( aResource("AccountTotals_2024_09.csv") );
		assertEquals( aBalanceOf(BARCLAYS_ACCOUNT, 1383.56F), accounts.get(0) );
		assertEquals( aBalanceOf(FIRSTDIRECT_ACCOUNT, 4145.63F), accounts.get(1) );
		assertEquals( aBalanceOf(GOLDFISH_ACCOUNT, -83.99F), accounts.get(2) );
	}

	private Account aBalanceOf(Account account, float expected) {
		return account.toBuilder()
				.balance(expected)
				.build();
	}

	private Path aResource(String name) throws URISyntaxException {
		final URL resourceUrl = this.getClass().getResource( name );
		assertNotNull( resourceUrl );

		return Paths.get(resourceUrl.toURI());
	}
}

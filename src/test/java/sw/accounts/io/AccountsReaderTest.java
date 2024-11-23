package sw.accounts.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sw.accounts.models.Account;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AccountsReaderTest
{
	private static final Account EXPECTED_BARCLAYS_FOR_AUGUST = Account.builder()
			.balance(1383.56F)
			.id("Barclays")
			.defaultTransactionType("Visa")
			.build();

	private static final Account EXPECTED_FIRSTDIRECT_FOR_AUGUST = Account.builder()
			.balance(4145.63F)
			.id("Firstdirect")
			.defaultTransactionType("")
			.build();

	private static final Account EXPECTED_GOLDFISH_FOR_AUGUST = Account.builder()
			.balance(-83.99F)
			.id("Goldfish")
			.defaultTransactionType("Mastercard")
			.build();

	@InjectMocks
	private AccountsReader testSubject;

	@Test
	void shouldLoadAccountTotals() throws Exception {
		final List<Account> accounts = testSubject.loadAccounts( aResource("AccountTotals_2024_08.csv") );
		assertEquals(EXPECTED_BARCLAYS_FOR_AUGUST, accounts.get(0) );
		assertEquals(EXPECTED_FIRSTDIRECT_FOR_AUGUST, accounts.get(1) );
		assertEquals(EXPECTED_GOLDFISH_FOR_AUGUST, accounts.get(2) );
	}

	private File aResource(String name) throws URISyntaxException {
		final URL resourceUrl = this.getClass().getResource( name );
		assertNotNull( resourceUrl );

		return Paths.get(resourceUrl.toURI()).toFile();
	}
}

package sw.accounts.io.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.jupiter.api.Test;

public class CsvTokeniserTest
{
	public static final String[] POCKET_MONEY_TOKENS = new String[] {
		"Account", "Date", "ChkNum", "Payee", "Category", "Class", "Memo", "Amount", "Cleared",
		"Barclays", "01/12/2009", "", "Morrisons", "Car:Petrol", "", "", "-88", "",
		"Barclays", "02/12/2009", "", "<Firstdirect>", "", "", "", "-2", "",
		"Firstdirect", "02/12/2009", "", "<Barclays>", "", "", "", "2", "",
		"Barclays", "13/12/2009", "", "Sharon Witchalls", "Sharon:Cash", "", "", "-123", "",
		"Barclays", "31/12/2009", "", "Sainsburys", "Car:Petrol", "", "Hello \"mummy\"", "-3", "*",
		"Barclays", "31/12/2009", "", "Sainsburys", "Car:Repair", "", "", "-4", "*",
		"Barclays", "30/11/2009", "", "Sainsburys", "Car:Repair", "", "", "-4", "",
		"Barclays", "01/01/2010", "", "Sharon Witchalls", "Sharon:Cash", "", "", "-123", ""
	};

	@Test
	void shouldLoadPocketMoneyFile() throws Exception {
		// files contains -
		// 1) Strings
		// 2) Empty fields, eg. [,,,]
		// 2) Empty last fields, eg. [text,]
		// 3) Embedded double-quotes
		
		final URL furl = this.getClass().getResource( "PocketMoney-20100113T225150.csv" );
		assertNotNull( furl );

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(furl.openStream()))) {
            int tokenPos = 0;

            String s;
            while ((s = reader.readLine()) != null) {
                final CsvTokeniser tokeniser = new CsvTokeniser(s);
                for (; tokeniser.hasMoreTokens(); tokenPos++) {
                    assertTrue(tokenPos < POCKET_MONEY_TOKENS.length);
                    assertEquals(POCKET_MONEY_TOKENS[tokenPos], tokeniser.nextToken());
                }
            }
        }
	}
}

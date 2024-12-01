package sw.accounts.io.pocketmoney;

import sw.accounts.io.TransactionsReader;
import sw.accounts.io.pocketmoney.csv.CatamountSoftwareCsvReader;
import sw.accounts.io.pocketmoney.csv.GmbhCsvReader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PocketMoneyConfig {

    @Bean("catamount-software-transactions-reader")
    public TransactionsReader getCatamountSoftwareTransactionsReader() {
        return new PocketMoneyTransactionsReader( new CatamountSoftwareCsvReader() );
    }

    @Bean("gmbh-transactions-reader")
    public TransactionsReader geGmbhTransactionsReader() {
        return new PocketMoneyTransactionsReader( new GmbhCsvReader() );
    }
}

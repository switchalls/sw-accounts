package sw.accounts.io.pocketmoney;

import sw.accounts.models.Transaction;

public interface TransactionReader {

    Transaction loadTransaction(String aContent) throws Exception;

}


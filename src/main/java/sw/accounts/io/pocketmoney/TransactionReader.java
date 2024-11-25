package sw.accounts.io.pocketmoney;

import sw.accounts.models.Transaction;

public interface TransactionReader {

    /**
     * Extract transaction information from a single record of data
     *
     * @param data The data, eg. CSV data
     * @return the transaction
     * @throws Exception when a problem occurs
     */
    Transaction loadTransaction(String data) throws Exception;

}


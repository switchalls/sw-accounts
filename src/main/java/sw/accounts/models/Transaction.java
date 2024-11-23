package sw.accounts.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Builder(toBuilder = true)
public class Transaction {

    public static final String CHEQUE = "Cheque";
    public static final String ELECTRONIC_TRANSFER = "Elec Trsf";
    public static final String SPLIT = "--SPLIT--";

    @Setter
    private String account;

    @Setter
    private float amount;

    @Setter
    private String payee;

    @Setter
    private String type;

    private String category;
    private String checkNumber;
    private String clazz;
    private boolean cleared;
    private Date date;
    private String memo;
    private String transferOther;

    public boolean isSplitTransaction(Transaction aOther) {
        return this.getAccount().equals(aOther.getAccount())
                && this.getDate().equals(aOther.getDate())
                && this.getPayee().equals(aOther.getPayee());
    }

    public boolean isSplitTransactionChild() {
        return SPLIT.equals(this.getPayee());
    }
}

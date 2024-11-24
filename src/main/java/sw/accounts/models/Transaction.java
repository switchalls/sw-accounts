package sw.accounts.models;

import lombok.*;

import java.util.Date;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class Transaction {

    public static final String CHEQUE = "Cheque";
    public static final String ELECTRONIC_TRANSFER = "Elec Trsf";
    public static final String SPLIT = "--SPLIT--";

    @Setter
    private float amount;

    @Setter
    private String type;

    private String account;
    private String category;
    private String clazz;
    private String checkNumber;
    private boolean cleared;
    private Date date;
    private String memo;
    private String payee;
    private String transferOther;

    public boolean isSplit() {
        return SPLIT.equals(this.getPayee());
    }

    public boolean isSplitOf(Transaction aOther) {
        return aOther != null
                && this.getAccount().equals(aOther.getAccount())
                && this.getDate().equals(aOther.getDate())
                && this.getPayee().equals(aOther.getPayee());
    }

    public void setAsSplit() {
        this.payee = Transaction.SPLIT;
        this.clazz = "";
        this.type = "";
    }
}

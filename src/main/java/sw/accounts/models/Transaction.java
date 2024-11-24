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
    private String account;

    @Setter
    private float amount;

    @Setter
    private String clazz;

    @Setter
    private String payee;

    @Setter
    private String type;

    private String category;
    private String checkNumber;
    private boolean cleared;
    private Date date;
    private String memo;
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
        this.setPayee( Transaction.SPLIT );
        this.setClazz( "" );
        this.setType( "" );
    }
}

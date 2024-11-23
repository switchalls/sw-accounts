package sw.accounts.models;

import lombok.*;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class Account {

    @Setter
    private float balance;

    private String defaultTransactionType;
    private String id;

}

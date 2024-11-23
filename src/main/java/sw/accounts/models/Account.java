package sw.accounts.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder(toBuilder = true)
public class Account {

    @Setter
    private float balance;

    private String defaultTransactionType;
    private String id;

}

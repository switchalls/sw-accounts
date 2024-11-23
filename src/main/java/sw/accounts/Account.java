package sw.accounts;

import java.io.Serializable;

public class Account implements Serializable {

	private static final long serialVersionUID = 76935591095077384L;

	private float balance;
	private String defaultTransactionType;
	private String id;
	
	public float getBalance()
	{
		return this.balance;
	}

	public void setBalance(float aBalance)
	{
		this.balance = aBalance;
	}

	public String getDefaultTransactionType()
	{
		return this.defaultTransactionType;
	}

	public void setDefaultTransactionType(String aType)
	{
		this.defaultTransactionType = aType;
	}

	public String getId()
	{
		return this.id;
	}

	public void setId(String aId)
	{
		this.id = aId;
	}

}

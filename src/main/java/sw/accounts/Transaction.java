package sw.accounts;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction implements Serializable {

	private static final long serialVersionUID = -6626664934139350771L;

	protected static final SimpleDateFormat DATE_AND_TIME_FORMAT = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );

	private String account;
	private float amount;
	private String category;
	private String checkNumber = "";
	private String clazz = "";
	private boolean cleared;
	private Date date;
	private String memo = "";
	private String payee;
	private String transferOther = "";
	private String type = "";

	public String getAccount()
	{
		return account;
	}
	
	public void setAccount(String aAccount)
	{
		this.account = aAccount;
	}

	public float getAmount()
	{
		return amount;
	}
	
	public void setAmount(float aAmount)
	{
		this.amount = aAmount;
	}
	
	public String getCategory()
	{
		return category;
	}
	
	public void setCategory(String aCategory)
	{
		this.category = aCategory;
	}
	
	public String getCheckNumber()
	{
		return checkNumber;
	}
	
	public void setCheckNumber(String aNumber)
	{
		this.checkNumber = aNumber;
	}
	
	public String getClazz() {
		return clazz;
	}
	
	public void setClazz(String aClazz)
	{
		this.clazz = aClazz;
	}
	
	public boolean isCleared()
	{
		return cleared;
	}
	
	public void setCleared(boolean aFlag)
	{
		this.cleared = aFlag;
	}
	
	public Date getDate()
	{
		return date;
	}
	
	public void setDate(Date aDate)
	{
		this.date = aDate;
	}
	
	public String getMemo()
	{
		return memo;
	}
	
	public void setMemo(String aMemo)
	{
		this.memo = aMemo;
	}
	
	public String getPayee()
	{
		return payee;
	}
	
	public void setPayee(String aPayee)
	{
		this.payee = aPayee;
	}
	
	public String getTransferOther()
	{
		return transferOther;
	}
	
	public void setTransferOther(String aFrom)
	{
		this.transferOther = aFrom;
	}

	public String getType()
	{
		return type;
	}
	
	public void setType(String aType)
	{
		this.type = aType;
	}

	public boolean isSplitTransaction(Transaction aOther) {
		return this.getAccount().equals(aOther.getAccount())
			&& this.getDate().equals(aOther.getDate())
			&& this.getPayee().equals(aOther.getPayee());
	}

	public boolean isSplitTransactionChild() {
		return AbstractReportGenerator.SPLIT.equals(this.getPayee());
	}
	
	public String toString() {
		return "[" + this.getAccount()
			+ "] " + DATE_AND_TIME_FORMAT.format(this.getDate())
			+ " [" + this.getPayee()
			+ "] " + this.getAmount();
	}

}

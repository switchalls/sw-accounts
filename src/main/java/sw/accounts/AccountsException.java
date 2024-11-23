package sw.accounts;

public class AccountsException extends Exception {

	private static final long serialVersionUID = 3139053184959882077L;

	public AccountsException(String aMsg)
	{
		super( aMsg );
	}
	
	public AccountsException(String aMsg, Throwable aCause)
	{
		super( aMsg, aCause );
	}

}

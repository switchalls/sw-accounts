package sw.accounts.exceptions;

public class AccountsException extends Exception {

	public AccountsException(String aMsg)
	{
		super( aMsg );
	}
	
	public AccountsException(String aMsg, Throwable aCause)
	{
		super( aMsg, aCause );
	}

}

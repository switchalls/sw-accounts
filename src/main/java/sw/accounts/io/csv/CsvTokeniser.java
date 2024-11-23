package sw.accounts.io.csv;

import java.io.IOException;

public class CsvTokeniser {
	private final String content;
	private String nextToken;
	private int nextTokenPos;
	private boolean emptyLastField;
	
	public CsvTokeniser(String aContent) {
		this.content = aContent;
		this.nextToken();
	}

	public boolean hasMoreTokens()
	{
		return (this.nextToken != null);
	}

	/**
	 * Get the next token.
	 * 
	 * @return the token or null (no more tokens available)
	 */
	public String nextToken() {
		final String t = this.nextToken;
		this.nextTokenPos = this.scanToken( this.nextTokenPos );
		return t;
	}

	/**
	 * Get the next mandatory token.
	 * 
	 * @param aField The name of the required token
	 * @return the token
	 * @throws IOException when the token cannot be read
	 */
	public String nextToken(String aField) throws IOException {
		if ( !this.hasMoreTokens() ) {
			throw new IOException("Invalid CSV file: missing "+aField+" field");				
		}
		return this.nextToken();
	}

	/**
	 * Extract the next token.
	 * 
	 * @param aStartPos The position to start scanning from
	 * @return the position of the field after the one read
	 */
	protected int scanToken(int aStartPos) {
		final int clen = this.content.length();		
		if ( aStartPos >= clen ) {
			if ( this.emptyLastField ) {
				this.emptyLastField = false;
				this.nextToken = "";
			}
			else {
				this.nextToken = null;
			}
			
			return aStartPos;
		}
		
		final StringBuilder field = new StringBuilder();
		boolean inString = false;
		boolean finished = false;
		int i = aStartPos;

		for (;  (i < clen) && !finished;  i++) {
			final char c = this.content.charAt( i );
			switch ( c ) {
			case ',':
				if ( inString )
				{					
					field.append( c );				
				}
				else
				{					
					if ( i+1 >= clen )
					{
						this.emptyLastField = true;
					}
					finished = true;
				}
				break;
			
			case '"':
				if ( !inString )
				{
					inString = true;
				}
				else if ( (i+1) < clen )
				{
					final char n = this.content.charAt(i+1);
					if ( n == '"' )
					{
						i++;
					}
					else
					{
						inString = false;
					}
				}

				field.append( c );
				break;
			
			default:
				field.append( c );
			}
		}
		
		String s = field.toString().trim();					
		if ( s.startsWith("\"") && s.endsWith( "\"") ) {
			s = s.substring( 1, s.length()-1 );
		}

		this.nextToken = s;

		return i;
	}

}

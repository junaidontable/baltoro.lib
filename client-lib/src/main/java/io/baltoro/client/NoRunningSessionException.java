package io.baltoro.client;

public class NoRunningSessionException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NoRunningSessionException(Throwable t) 
	{
		super(t);
	}

	public NoRunningSessionException() 
	{
		super();
	}

}

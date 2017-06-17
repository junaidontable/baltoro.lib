package io.baltoro.client;

public class SendRedirect extends RuntimeException
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private final String url;
	
	public SendRedirect(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}
}

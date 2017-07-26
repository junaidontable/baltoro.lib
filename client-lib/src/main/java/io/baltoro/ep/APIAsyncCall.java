package io.baltoro.ep;

import java.util.concurrent.Callable;

import javax.websocket.Session;

class APIAsyncCall implements Callable<Session>
{
	
	 
	APIAsyncCall()
	{
		
	}
	
	
	@Override
	public Session call() throws Exception
	{
		try 
	    {
			
			
	 	  return null ;
	 	 
	    }
	    catch (Exception e) 
	    {
	        throw new IllegalStateException("task interrupted", e);
	    }
	}
	
	
	

}

package io.baltoro;

import java.util.concurrent.Callable;

import io.baltoro.util.Log;

public class RequestWorker implements Callable<String>
{
	
	private String json;
	
	
	public RequestWorker(String json)
	{
		this.json = json;
	}
	
	@Override
	public String call() throws Exception
	{
		Log.log.info(json);
		return null;
	}
}
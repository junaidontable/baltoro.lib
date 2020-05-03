package io.baltoro;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import io.baltoro.util.Log;
import io.baltoro.util.StringUtil;
import io.baltoro.util.UUID;

public class Baltoro
{

	static String domain = "baltoro.io";
	static String protocol = "https";
	static String POLL_URL = protocol+"://poll."+domain;
	
	static String API_KEY;
	static String AUTH_CODE;
	static String INST_UUID;
	static String POLL_UUID;
	static int WORKER_COUNT = 10;
	
	
	
	public static ThreadPoolExecutor workers;
	
	
	public Baltoro()
	{
		try
		{
			init();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args)
	{
		new Baltoro();
	}
	
	private void init() throws Exception
	{
		
		Log.log.info(" ..... in main .....");
		
		
		String apiKey = System.getProperty("apiKey");
		String authCode = System.getProperty("authCode");
		
		if(StringUtil.isNullOrEmpty(apiKey) || StringUtil.isNullOrEmpty(authCode))
		{
			Log.log.info("apiKey or authCode is not set, exiting .... ");
			System.exit(1);
		}
	
		API_KEY = apiKey;
		AUTH_CODE = authCode;
		INST_UUID = UUID.genUuid();
		
		APIProcessor p = new APIProcessor();
		
		Map<String, APIMethod> pathMap = p.processAnnotation();
		APIMap.getInstance().setMap(pathMap);
		
		Log.log.info(" ......... reflection ........");
		
		
		POLL_UUID = WebClient.instance().ping();
		
		Log.log.info("POLL_UUID = "+POLL_UUID);
		
		workers = (ThreadPoolExecutor) Executors.newFixedThreadPool(WORKER_COUNT);
		
		PollWorker pollWorker = new PollWorker(this, 1);
		
		pollWorker.start();
		
		

	}
	
	
}

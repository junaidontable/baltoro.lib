package io.baltoro.client;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Baltoro 
{
	
	static Logger log = Logger.getLogger(Baltoro.class.getName());
	
	private Baltoro(String appId, String[] packages)
	{
		//System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT:%4$s > %5$s%6$s%n");
		
		try
		{
			init(appId, packages);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private Baltoro(String appId, String _package)
	{
		//System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT:%4$s > %5$s%6$s%n");
		
		try
		{
			init(appId, new String[]{_package});
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	private void init(String appId, String[] packages) throws Exception
	{
		
		Map<String, WebMethod> pathMap = new HashMap<String, WebMethod>(200);
		
		AnnotationProcessor p = new AnnotationProcessor();
		for (String _package : packages)
		{
			Map<String, WebMethod> pMap = p.processAnnotation(_package);
			pathMap.putAll(pMap);
		}
		
		WebMethodMap.getInstance().setMap(pathMap);
		
		BaltoroWSClient client = new BaltoroWSClient();
		client.start(appId);
		
		
	}
	
	public static void start(String appId, String[] packages)
	{
		new Baltoro(appId, packages);
	}
	
	public static void start(String appId, String _package)
	{
		new Baltoro(appId, _package);
	}
	
    public static void main( String[] args )
    {
        new Baltoro("baltoro-test","io.baltoro.client.test");
    }
}

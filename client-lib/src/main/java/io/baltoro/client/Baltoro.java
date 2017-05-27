package io.baltoro.client;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.baltoro.ep.ClassBuilder;

public class Baltoro 
{
	
	static Logger log = Logger.getLogger(Baltoro.class.getName());
	
	private static Map<String, Class<?>> classMap = new HashMap<String, Class<?>>(); 
	
	private Baltoro(String appId, String[] packages)
	{
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT:%4$s > %5$s%6$s%n");
		
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
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT:%4$s > %5$s%6$s%n");
		
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
		
		BaltoroWSClient client = new BaltoroWSClient(appId);
		client.start();
		
		
	}
	
	
	public static <T> T EndPointFactory(Class<T> _class)
	{
		try
		{
			Class implClass = classMap.get(_class.getName());
			if(implClass == null)
			{
				ClassBuilder builder = new ClassBuilder(_class);
				implClass = builder.buildClass();
				classMap.put(_class.getName(), implClass);
			}
			
			//Class<?> implClass = Class.forName(_class.getName()+"Impl");
			Object obj = implClass.newInstance();
			return (T)obj;
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
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

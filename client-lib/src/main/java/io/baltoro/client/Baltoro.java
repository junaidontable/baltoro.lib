package io.baltoro.client;

import java.util.logging.Logger;

public class Baltoro 
{
	
	static Logger log = Logger.getLogger(Baltoro.class.getName());
	
	public Baltoro(String[] packages)
	{
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT:%4$s > %5$s%6$s%n");
		
		try
		{
			init(packages);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Baltoro(String _package)
	{
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT:%4$s > %5$s%6$s%n");
		
		try
		{
			init(new String[]{_package});
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	private void init(String[] packages) throws Exception
	{
		
		AnnotationProcessor p = new AnnotationProcessor();
		for (String _package : packages)
		{
			p.processAnnotation(_package);
		}
		
	}
	
    public static void main( String[] args )
    {
    	String[] packages = new String[]{"io.baltoro.client.test"};
        new Baltoro(packages);
    }
}

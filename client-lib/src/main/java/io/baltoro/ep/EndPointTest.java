package io.baltoro.ep;

import io.baltoro.client.Baltoro;

public class EndPointTest
{

	public static void main(String[] args)
	{
		try
		{
			TestEndpointCall1 impl = Baltoro.EndPointFactory(TestEndpointCall1.class);
			
			System.out.println("implClass === > "+impl);
			
			String ret = impl.hello();
	        
	        System.err.println(" ---- > "+ret);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	
}

package io.baltoro.client.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;


public class StreamUtil
{
	

	public static byte[] toBytes(InputStream in) throws Exception
	{
		BufferedInputStream bis = new BufferedInputStream(in);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(out);
		
		try
		{
			while(bis.available()>0) 
			{
				byte[] data = new byte[1024];
	            
	            int bytesRead=0;
	           
	            while( (bytesRead = bis.read(data)) != -1)
	            {
	            	bos.write(bytesRead);
	            }
	            
	        }
		} 
		finally
		{
			try
			{
				bis.close();
				bos.close();
				out.close();
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
		}
		return out.toByteArray();
	}
	
	public static byte[] toBytes(Object obj) throws Exception
	{
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(obj);
	    return out.toByteArray();
		
	}
}

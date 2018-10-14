package io.baltoro.client.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;


public class StreamUtil
{
	

	public static byte[] toBytes(InputStream in) throws Exception
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int len;

		try
		{
			while ((len = in.read(buffer)) != -1) 
			{
				os.write(buffer, 0, len);
			}
		}
		catch(Exception e)
		{
			return null;
		}

		return os.toByteArray();
	}
	
	public static byte[] toBytes(Object obj) throws Exception
	{
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(obj);
	    return out.toByteArray();
		
	}
}

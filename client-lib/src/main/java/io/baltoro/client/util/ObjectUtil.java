package io.baltoro.client.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectUtil
{
	static ObjectMapper mappper = new ObjectMapper();
	

	public static byte[] toJason(Object obj) throws Exception
	{
		byte[] bytes = mappper.writeValueAsBytes(obj);
		return bytes;
	}

	
	public static <T> T toObject(Class<?> clazz, byte[] bytes)
	{
		Object obj = null;
		try
		{
			obj = mappper.readValue(bytes, clazz);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return (T)obj;
	}
		
}

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
	
	public static String toJasonStr(Object obj) throws Exception
	{
		return mappper.writeValueAsString(obj);
		
	}

	public static String getType(String objUuid)
	{
		return objUuid.substring(objUuid.length()-4,objUuid.length());
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
			System.out.println(new String(bytes));
			e.printStackTrace();
		}
		
		return (T)obj;
	}
		
}

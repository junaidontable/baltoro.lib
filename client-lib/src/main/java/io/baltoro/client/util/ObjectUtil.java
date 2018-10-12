package io.baltoro.client.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

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
	
	public static byte[] convertToBytes(Object object) 
	{
	    try (
	    	 ByteArrayOutputStream bos = new ByteArrayOutputStream();
	         ObjectOutput out = new ObjectOutputStream(bos)
	        ) 
	    {
	        out.writeObject(object);
	        return bos.toByteArray();
	    } 
	    catch (Exception e) 
	    {
			e.printStackTrace();
		}
	    
	    return null;
	}
	
	public static Object convertFromBytes(byte[] bytes)
	{
	    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	         ObjectInput in = new ObjectInputStream(bis)) 
	    {
	        return in.readObject();
	    } 
	    catch (Exception e) 
	    {
			e.printStackTrace();
		}
	    
	    return null;
	}
		
}

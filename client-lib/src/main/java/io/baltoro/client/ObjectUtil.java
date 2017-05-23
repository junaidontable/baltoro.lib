package io.baltoro.client;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectUtil
{

	

	public static byte[] toJason(Object obj) throws Exception
	{
		ObjectMapper mappper = new ObjectMapper();
		byte[] bytes = mappper.writeValueAsBytes(obj);
		return bytes;
	}
		
}

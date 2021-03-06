package io.baltoro.client.util;

import java.util.UUID;

public class UUIDGenerator 
{
	
	public static String uuid(String type) 
	{
		return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase()+"_"+type;
	}
	
	public static String randomString(int len) 
	{
		return UUID.randomUUID().toString().replaceAll("-", "").substring(0, len).toUpperCase();
	}

}

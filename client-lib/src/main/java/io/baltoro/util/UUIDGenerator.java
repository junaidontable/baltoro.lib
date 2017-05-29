package io.baltoro.util;

import java.util.UUID;

public class UUIDGenerator 
{
	
	public static String uuid(String type) 
	{
		return type+"-"+UUID.randomUUID().toString().toUpperCase();
	}
	
	public static String randomString(int len) 
	{
		return UUID.randomUUID().toString().substring(0, len).toUpperCase();
	}

}

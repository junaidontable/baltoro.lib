package io.baltoro.client.util;

import java.util.Base64;

public class StringUtil 
{
	public static boolean isNullOrEmpty(String str)
	{
		if(str != null && str.length() > 0)
		{
			return false;
		}
		
		return true;
			
	}
	
	public static boolean isNotNullAndNotEmpty(String str)
	{
		if(str != null && str.length() > 0)
		{
			return true;
		}
		
		return false;	
	}
	
	
	public static String stripPhoneNumber(String phoneNumber)
	{
		StringBuffer str = new StringBuffer();
		
		char[] chars = phoneNumber.toCharArray();
		for (int i = 0; i < chars.length; i++) 
		{
			char c = chars[i];
			if(c >= '0' && c <= '9')
			{
				str.append(chars[i]);
			}
		}
		return str.toString();
	}

	
	
	public static String encode(byte[] bytes)
	{
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	public static byte[] decode(String str)
	{
		return Base64.getDecoder().decode(str);
	}
}

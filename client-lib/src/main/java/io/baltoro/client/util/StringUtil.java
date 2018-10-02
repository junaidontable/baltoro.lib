package io.baltoro.client.util;

import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;

import io.baltoro.obj.Base;

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
	
	public static boolean isNullOrEmpty(Collection<?> col)
	{
		if(col != null && col.size() > 0)
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
	
	
	public static String[] toUuids(Base[] objs)
	{
		String[] uuids = new String[objs.length];
		
		for (int i=0;i<objs.length;i++) 
		{
			if(objs[i] == null)
			{
				uuids[i] = null;
			}
			else
			{
				uuids[i] = objs[i].getBaseUuid();
			}
		}
	
		
		return uuids;
	}
	
	
	public static String toInClause(String[] array)
	{
		StringBuilder buffer = new StringBuilder(array.length * 10); 
		for (String val : array) 
		{
			buffer.append("'"+val+"',");
		}
		buffer.deleteCharAt(buffer.length()-1);
		
		return buffer.toString();
	}
	
	public static String toInClause(Base... bases)
	{
		StringBuilder buffer = new StringBuilder(bases.length * 10); 
		for (Base base : bases) 
		{
			buffer.append("'"+base.getBaseUuid()+"',");
		}
		buffer.deleteCharAt(buffer.length()-1);
		
		return buffer.toString();
	}

	
	public static String toInClause(Collection<String> col)
	{
		if(col==null || col.isEmpty())
		{
			return "";
		}
		
		StringBuilder buffer = new StringBuilder(col.size() * 10); 
		Iterator<String> it = col.iterator();
		while(it.hasNext())
		{
			String val = it.next();
			buffer.append("'"+val+"',");
		}
		buffer.deleteCharAt(buffer.length()-1);
		
		return buffer.toString();
	}
	
	public static String toInClauseForMetadata(Collection<Base> col)
	{
		if(col==null || col.isEmpty())
		{
			return "";
		}
		
		StringBuilder buffer = new StringBuilder(col.size() * 10); 
		Iterator<Base> it = col.iterator();
		while(it.hasNext())
		{
			Base val = it.next();
			buffer.append("'"+val.getLatestVersionUuid()+"',");
		}
		buffer.deleteCharAt(buffer.length()-1);
		
		return buffer.toString();
	}
	
	
}

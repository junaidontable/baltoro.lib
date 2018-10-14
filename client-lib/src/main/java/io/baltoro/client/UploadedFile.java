package io.baltoro.client;

public class UploadedFile
{

	private String uuid;
	private String name;
	private byte[] data;
	private long size;
	
	public String getUuid()
	{
		return uuid;
	}
	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public byte[] getData()
	{
		return data;
	}
	
	public long getSize()
	{
		return size;
	}
	public void setSize(long size)
	{
		this.size = size;
	}
	
	
	
	
}

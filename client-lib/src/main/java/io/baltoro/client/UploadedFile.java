package io.baltoro.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UploadedFile
{

	private String uuid;
	private String name;
	private byte[] data;
	private long size;
	private String contentType;
	
	public String getUuid()
	{
		return uuid;
	}
	
	void setUuid(String uuid)
	{
		this.uuid = uuid;
	}
	public String getName()
	{
		return name;
	}
	
	void setName(String name)
	{
		this.name = name;
	}

	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public Future<byte[]> getData() 
	{       

		return executor.submit(() -> 
        {
        	if(data != null)
        	{
        		return data;
        	}
            
        	data = Baltoro.cs.pullUploadedFileData(uuid);
            return data;
        });
	}

	
	public long getSize()
	{
		return size;
	}
	void setSize(long size)
	{
		this.size = size;
	}
	public String getContentType()
	{
		return contentType;
	}
	void setContentType(String contentType)
	{
		this.contentType = contentType;
	}
	
	
	
	
}

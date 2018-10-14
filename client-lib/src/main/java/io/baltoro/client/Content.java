package io.baltoro.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.baltoro.features.Store;
import io.baltoro.obj.Base;

public class Content extends Base
{

	private @Store String serverUuid;
	private byte[] data;
	private @Store long size;
	private @Store String contentType;
	private @Store long uploadedOn;
	
	public String getServerUuid()
	{
		return serverUuid;
	}
	
	void setServerUuid(String serverUuid)
	{
		this.serverUuid = serverUuid;
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
            
        	data = Baltoro.cs.pullUploadedFileData(getServerUuid());
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

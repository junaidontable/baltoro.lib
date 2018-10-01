package io.baltoro.to;

public class WebSocketContext
{
	private String initRequestUuid;
	private String wsSessionUuid;
	private String apiPath;
	private byte[] data;
	private String message;
	
	
	public String getInitRequestUuid()
	{
		return initRequestUuid;
	}
	public void setInitRequestUuid(String initRequestUuid)
	{
		this.initRequestUuid = initRequestUuid;
	}

	public String getWsSessionUuid()
	{
		return wsSessionUuid;
	}
	public void setWsSessionUuid(String wsSessionUuid)
	{
		this.wsSessionUuid = wsSessionUuid;
	}
	public byte[] getData()
	{
		return data;
	}
	public void setData(byte[] data)
	{
		this.data = data;
	}
	public String getApiPath()
	{
		return apiPath;
	}
	public void setApiPath(String apiPath)
	{
		this.apiPath = apiPath;
	}
	public String getMessage()
	{
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
	
	
}

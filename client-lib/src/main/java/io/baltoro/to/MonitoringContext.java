package io.baltoro.to;

public class MonitoringContext
{
	private String hostName;
	private int cpuPercent;
	private int memoryGB;
	private int heartBeatCount;
	private long localTimestamp;
	
	
	public int getCpuPercent()
	{
		return cpuPercent;
	}
	public void setCpuPercent(int cpuPercent)
	{
		this.cpuPercent = cpuPercent;
	}
	public int getMemoryGB()
	{
		return memoryGB;
	}
	public void setMemoryGB(int memoryGB)
	{
		this.memoryGB = memoryGB;
	}
	public String getHostName()
	{
		return hostName;
	}
	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}
	public int getHeartBeatCount()
	{
		return heartBeatCount;
	}
	public void setHeartBeatCount(int heartBeatCount)
	{
		this.heartBeatCount = heartBeatCount;
	}
	public long getLocalTimestamp()
	{
		return localTimestamp;
	}
	public void setLocalTimestamp(long localTimestamp)
	{
		this.localTimestamp = localTimestamp;
	}
	
	
	

}

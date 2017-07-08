package io.baltoro.bto;

import java.util.List;

public class MgntContext
{
	private String hostName;
	private int cpuPercent;
	private int memoryGB;
	private int heartBeatCount;
	private long localTimestamp;
	private String clusterPath;
	private List<PathTO> pathTOs;
	
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
	public List<PathTO> getPathTOs()
	{
		return pathTOs;
	}
	public void setPathTOs(List<PathTO> pathTOs)
	{
		this.pathTOs = pathTOs;
	}
	public String getClusterPath()
	{
		return clusterPath;
	}
	public void setClusterPath(String clusterPath)
	{
		this.clusterPath = clusterPath;
	}
	
	
	
	
	

}

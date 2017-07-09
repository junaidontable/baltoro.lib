package io.baltoro.bto;

import java.util.List;

import io.baltoro.bto.PathTO;

public class MgntContext
{
	
	private int cpuPercent;
	private int memoryGB;
	private int heartBeatCount;
	private long localTimestamp;
	private String clusterPath;
	private List<PathTO> pathTOs;
	private int threadCount;
	
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
	public int getThreadCount()
	{
		return threadCount;
	}
	public void setThreadCount(int threadCount)
	{
		this.threadCount = threadCount;
	}
	
	
	
	
	

}

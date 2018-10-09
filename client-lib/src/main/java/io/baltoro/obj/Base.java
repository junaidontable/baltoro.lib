package io.baltoro.obj;

import java.sql.Timestamp;


public class Base
{
	private String containerUuid = BODefaults.BASE_CONTAINER;
	private String baseUuid;
	private String versionUuid;
	private int versionNumber = 1;
	private String name;
	private String type;
	private String state = "LIVE";
	private String latestVersionUuid;
	private String permissionType = "CONT";
	private String createdBy = BODefaults.BASE_USER;
	private Timestamp createdOn = new Timestamp(System.currentTimeMillis());
	
	
	public String getUuid()
	{
		return baseUuid;
	}
	
	public void setUuid(String uuid)
	{
		this.baseUuid = uuid;
	}
	
	public String getContainerUuid()
	{
		return containerUuid;
	}
	public void setContainerUuid(String containerUuid)
	{
		this.containerUuid = containerUuid;
	}
	
	public int getVersionNumber()
	{
		return versionNumber;
	}
	public void setVersionNumber(int versionNumber)
	{
		this.versionNumber = versionNumber;
	}
	
	public String getBaseUuid()
	{
		return baseUuid;
	}
	public void setBaseUuid(String baseUuid)
	{
		this.baseUuid = baseUuid;
	}
	
	public String getVersionUuid()
	{
		return versionUuid;
	}
	public void setVersionUuid(String versionUuid)
	{
		this.versionUuid = versionUuid;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	
	
	public String getCreatedBy()
	{
		return createdBy;
	}
	public void setCreatedBy(String createdBy)
	{
		this.createdBy = createdBy;
	}
	
	public Timestamp getCreatedOn()
	{
		return createdOn;
	}
	public void setCreatedOn(Timestamp createdOn)
	{
		this.createdOn = createdOn;
	}
	
	public String getState() 
	{
		return state;
	}
	public void setState(String state) 
	{
		this.state = state;
	}
	
	public String getType() 
	{
		return type;
	}
	public void setType(String type) 
	{
		this.type = type;
	}
	
	public String getLatestVersionUuid() 
	{
		return latestVersionUuid;
	}
	public void setLatestVersionUuid(String latestVersionUuid) 
	{
		this.latestVersionUuid = latestVersionUuid;
	}
	public String getPermissionType() 
	{
		return permissionType;
	}
	public void setPermissionType(String permissionType) 
	{
		this.permissionType = permissionType;
	}
	
	@Override
	public int hashCode()
	{

       // return baseUuid.hashCode() ^ session.getId().hashCode();
		return baseUuid.hashCode();
    }
	
	@Override
	public String toString()
	{
		return baseUuid+", "+name;
	}
	
	
	
}

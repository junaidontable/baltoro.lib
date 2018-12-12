package io.baltoro.to;

import java.sql.Timestamp;


public class App
{

	private String  uuid;
	private String name;
	private String state;
	private String env;
	private Timestamp createdOn;
	
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
	public String getEnv()
	{
		return env;
	}
	public void setEnv(String env)
	{
		this.env = env;
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
	
	
	
	
	
	
}

package io.baltoro.client;

public class ServicePackage
{

	String serviceName;
	String[] packageNames;
	
	
	public ServicePackage(String serviceName, String ... packageNames)
	{
		this.serviceName = serviceName;
		this.packageNames = packageNames;
		
	}
	
	
	
}

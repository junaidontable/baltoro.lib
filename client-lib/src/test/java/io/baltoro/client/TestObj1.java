package io.baltoro.client;

import io.baltoro.features.Store;
import io.baltoro.obj.Base;

public class TestObj1 extends Base
{

	private @Store String att1;
	private @Store int att2;
	private @Store String att3;
	
	
	public String getAtt1()
	{
		return att1;
	}
	public void setAtt1(String att1)
	{
		this.att1 = att1;
	}
	public int getAtt2()
	{
		return att2;
	}
	public void setAtt2(int att2)
	{
		this.att2 = att2;
	}
	public String getAtt3()
	{
		return att3;
	}
	public void setAtt3(String att3)
	{
		this.att3 = att3;
	}
	
	
	
}

package io.baltoro.obj;

import io.baltoro.db.Base;
import io.baltoro.db.Store;

public class User extends Base
{

	private @Store String salt;
	private @Store String password;


	public String getSalt()
	{
		return salt;
	}

	public void setSalt(String salt)
	{
		this.salt = salt;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getUserUuid()
	{
		return getName();
	}

	public void setUserUuid(String userUuid)
	{
		setName(userUuid);
	}
	
	
	
	
	
}

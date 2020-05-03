package io.baltoro.obj;

import io.baltoro.db.Base;
import io.baltoro.db.Store;

public class Tennant extends Base
{

	private @Store String  pubKeyUuid;

	public String getPubKeyUuid()
	{
		return pubKeyUuid;
	}

	public void setPubKeyUuid(String pubKeyUuid)
	{
		this.pubKeyUuid = pubKeyUuid;
	}
	
	
}

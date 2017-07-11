package io.baltoro.features;

import io.baltoro.to.WSTO;
import io.baltoro.client.UserSession;

public abstract class AbstractFilter
{

	public abstract void before(WSTO to, UserSession userSession);
	
	public abstract void after(Object resturnObj,  WSTO to, UserSession userSession);
}

package io.baltoro.client;

import javax.websocket.Session;

public class ClientWSSession
{
	private final Session session;
	private boolean working = false;
	
	
	public ClientWSSession(Session session)
	{
		this.session = session;
	}
	
	public Session getSession()
	{
		return session;
	}

	public boolean isWorking()
	{
		return working;
	}
	public void setWorking(boolean working)
	{
		this.working = working;
	}
	
	public int hashCode()
	{

        return session.getId().hashCode();
    }

    public boolean equals(Object obj)
    {
    	if(obj instanceof ClientWSSession)
    	{
    		ClientWSSession _sess = (ClientWSSession) obj;
    		return session.getId().equals(_sess.getSession().getId());
    	}
    	else
    	{
    		return super.equals(obj);
    	}
    }
	
}

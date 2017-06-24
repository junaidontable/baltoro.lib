package io.baltoro.client;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.websocket.Session;

import io.baltoro.client.util.StringUtil;


public class WSSessions
{

	private static WSSessions sessions;
	private Set<ClientWSSession> set;
	private ConcurrentLinkedQueue<ByteBuffer> requestQueue;
	private ConcurrentLinkedQueue<ByteBuffer> responseQueue;
	
	private WSSessions()
	{
		set = new HashSet<>();
		requestQueue = new ConcurrentLinkedQueue<>();
		responseQueue = new ConcurrentLinkedQueue<>();
		
	}
	
	public static WSSessions get()
	{
		if(sessions == null)
		{
			sessions = new WSSessions();
		}
		return sessions;
	}
	
	ConcurrentLinkedQueue<ByteBuffer> getRequestQueue()
	{
		return requestQueue;
	}
	
	ConcurrentLinkedQueue<ByteBuffer> getResponseQueue()
	{
		return responseQueue;
	}
	
	public void addSession(ClientWSSession session)
	{
		String sync = "session-queue";
		synchronized (sync.intern())
		{
			set.add(session);
			sync.intern().notify();
		}
		
	}
	
	void addToRequestQueue(ByteBuffer byteBuffer)
	{
		
		requestQueue.add(byteBuffer);
		
		String sync = "request-queue";
		synchronized (sync.intern())
		{
			sync.intern().notify();
		}
		
	}
	
	
	void addToResponseQueue(ByteBuffer byteBuffer)
	{
		
		responseQueue.add(byteBuffer);
		
		String sync = "response-queue";
		synchronized (sync.intern())
		{
			sync.intern().notify();
		}
		
	}


	
	ClientWSSession getSession()
	throws NoRunningSessionException
	{
		if(StringUtil.isNullOrEmpty(set))
		{
			throw new NoRunningSessionException();
		}
		
	
		String sync = "session-queue";
		synchronized (sync.intern())
		{
			for (ClientWSSession session : set)
			{
				if(session.isWorking() == false)
				{
					return session;
				}
			}
		}
		
		return null;
	}
	
	
	
	
	public void removeSession(Session session)
	{
		ClientWSSession rm = null;
		String sync = "session-queue";
		synchronized (sync.intern())
		{
			for (ClientWSSession cs : set)
			{
				if(cs.getSession().getId().equals(session.getId()))
				{
					rm = cs;
				}
			}
			
			if(rm != null)
			{
				set.remove(rm);
			}
		}
	}
}

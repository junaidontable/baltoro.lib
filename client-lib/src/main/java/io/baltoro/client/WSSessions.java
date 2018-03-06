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
	private Set<Session> set;
	private ConcurrentLinkedQueue<ByteBuffer> requestQueue;
	private ConcurrentLinkedQueue<ByteBuffer> responseQueue;
	
	private Set<Session> busySessions;
	private ConcurrentLinkedQueue<Session> freeSessions;
	
	private WSSessions()
	{
		set = new HashSet<>();
		requestQueue = new ConcurrentLinkedQueue<>();
		responseQueue = new ConcurrentLinkedQueue<>();
		
		busySessions = new HashSet<>();
		freeSessions = new ConcurrentLinkedQueue<>();
		
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
	
	public void addSession(Session session)
	{
		freeSessions.add(session);
		set.add(session);
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


	int checkSessions()
	{
		if(StringUtil.isNullOrEmpty(set))
		{
			return 0;
		}
		else
		{
			return set.size();
		}
	}
	
	Session getSessionForWork()
	{
		if(StringUtil.isNullOrEmpty(set))
		{
			freeSessions.clear();
			busySessions.clear();
			return null;
		}
		
		if(freeSessions.isEmpty())
		{
			return null;
		}
		
		Session  session = freeSessions.poll();
		busySessions.add(session);
		
		return session;

	}
	
	void releaseSession(Session session)
	{
		freeSessions.add(session);
		busySessions.remove(session);
		
		String sync = "response-queue";
		synchronized (sync.intern())
		{
			sync.intern().notifyAll();
		}
	}
	
	
	public void removeSession(Session session)
	{
		
		set.remove(session);
		busySessions.remove(session);
		freeSessions.remove(session);
		
		if(set.size() == 0)
		{
			System.out.println("No running sessions plz restart the instance ");
			System.exit(1);
		}
		
		
	}
}

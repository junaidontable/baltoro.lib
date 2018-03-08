package io.baltoro.client;

import java.nio.ByteBuffer;

import javax.websocket.Session;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseWorker extends  Thread
{
	private ByteBuffer byteBuffer;
	private Session session;

	static ObjectMapper objectMapper = new ObjectMapper();
	
	boolean run = true;
	static int _count;
	int count;
	
	public ResponseWorker()
	{
		synchronized (ResponseWorker.class.getName().intern())
		{
			this.count = _count++;
		}
		
	}
	
	void set(ByteBuffer byteBuffer, Session session)
	{
		this.byteBuffer = byteBuffer;
		this.session = session;
		
		synchronized (this)
		{
			this.notify();
		}
	}
	
	void clear()
	{
		this.byteBuffer = null;
		this.session = null;
	}
	
	@Override
	public void run()
	{
		while (run)
		{
			if(byteBuffer == null)
			{
				synchronized (this)
				{
					try
					{
						//System.out.println("worker before waiting ..... "+this+",  --- "+count);
						
						this.wait(10000);
						
						//System.out.println("worker after waiting ..... "+this+",  --- "+count);
						
						if(byteBuffer == null || session == null)
						{
							System.out.println("RESPONSE thread no work to do  "+this+",  --- "+count+",,,"+WorkerPool.info());
							continue;
						}
					} 
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
			
			try
			{
				session.getBasicRemote().sendBinary(byteBuffer);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally 
			{
				WSSessions.get().releaseSession(session);
				session = null;
				byteBuffer = null;
				WorkerPool.done(this);
			}
			
		}
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		return count == ((ResponseWorker)obj).count;
	}
	
}
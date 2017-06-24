package io.baltoro.client;

import java.nio.ByteBuffer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseWorker extends  Thread
{
	private ByteBuffer byteBuffer;
	private ClientWSSession session;

	static ObjectMapper objectMapper = new ObjectMapper();

	
	public ResponseWorker(ByteBuffer byteBuffer, ClientWSSession session)
	{
		this.byteBuffer = byteBuffer;
		this.session = session;
	}
	
	public void run()
	{
		
			
			try
			{
				
				session.setWorking(true);
				System.out.println("......... ////////////// ......... sending byte buffer ");
				session.getSession().getBasicRemote().sendBinary(byteBuffer);
				session.setWorking(false);
				
				String sync = "response-queue";
				synchronized (sync.intern())
				{
					sync.intern().notify();
				}
				
				
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		
	}
}
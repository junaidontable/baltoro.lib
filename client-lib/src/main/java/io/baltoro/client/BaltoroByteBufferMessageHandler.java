package io.baltoro.client;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

public class BaltoroByteBufferMessageHandler implements MessageHandler.Whole<ByteBuffer>
{
	
	static Logger log = Logger.getLogger(BaltoroByteBufferMessageHandler.class.getName());
	
	private Session session;
	private String appUuid;
	private String instanceUuid;
	
	public BaltoroByteBufferMessageHandler(String appUuid, String instanceUuid, Session session)
	{
		this.session = session;
		this.appUuid = appUuid;
		this.instanceUuid = instanceUuid;
	}

	@Override
	public void onMessage(ByteBuffer byteBuffer)
	{
		RequestQueue.instance().addToRequestQueue(byteBuffer);
	}
}


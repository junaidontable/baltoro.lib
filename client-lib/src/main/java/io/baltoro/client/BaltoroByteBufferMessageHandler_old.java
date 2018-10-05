package io.baltoro.client;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

public class BaltoroByteBufferMessageHandler_old implements MessageHandler.Whole<ByteBuffer>
{
	
	static Logger log = Logger.getLogger(BaltoroByteBufferMessageHandler_old.class.getName());
	
	private Session session;
	private String appUuid;
	private String instanceUuid;
	
	public BaltoroByteBufferMessageHandler_old(String appUuid, String instanceUuid, Session session)
	{
		this.session = session;
		this.appUuid = appUuid;
		this.instanceUuid = instanceUuid;
	}

	@Override
	public void onMessage(ByteBuffer byteBuffer)
	{
		//RequestQueue.instance().addToRequestQueue(byteBuffer);
	}
}


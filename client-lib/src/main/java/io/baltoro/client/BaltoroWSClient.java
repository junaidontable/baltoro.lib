package io.baltoro.client;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;

import org.glassfish.tyrus.client.ClientManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.to.WSTO;


@ClientEndpoint
public class BaltoroWSClient extends Thread
{

	private static CountDownLatch latch;
	private Logger log = Logger.getLogger(this.getClass().getName());
	private String appId;
	
	BaltoroWSClient(String appId)
	{
		this.appId = appId;
	}
	
	
	
	@OnOpen
	public void onOpen(Session session)
	{
		log.info("Connected ... " + session.getId());
      
		try
		{
			session.getBasicRemote().sendText("start");
			BaltoroWSPing thread = new BaltoroWSPing(latch, session);
			thread.start();
			//latch.countDown();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@OnMessage
	//public String onMessage(String message, Session session)
	public String onMessage(ByteBuffer bytesBuffer, Session session)
	{
		log.info(" appid --- > text");
		
		
		try
		{
			byte[] jsonBytes = bytesBuffer.array();
			
			ObjectMapper mapper = new ObjectMapper();
			WSTO to = mapper.readValue(jsonBytes,  WSTO.class);
			
			Map<String, String[]> requestParam = to.requestParams;
			if(requestParam == null || requestParam.size()==0)
			{
				requestParam = new HashMap<String, String[]>();
			}
			
			WebMethod wMethod = WebMethodMap.getInstance().getMethod(to.path);
			Class<?> _class = wMethod.get_class();
			Method method = wMethod.getMethod();
			
			boolean noParam = true;
			Parameter[] methodParms = method.getParameters();
			Object[] methodInputData = new Object[methodParms.length];
			
			
			
			for (int i = 0; i < methodParms.length; i++)
			{
				noParam = true;
				Parameter param = methodParms[i];
				Class<?> paramClass = param.getType();
				
				
				String annoName = null;
				Annotation[] annos = param.getAnnotations();
				for (int j = 0; j < annos.length; j++)
				{
					Annotation anno = annos[j];
					if(anno.annotationType() == QueryParam.class)
					{
						QueryParam annoPraram = (QueryParam) anno;
						annoName = annoPraram.value();
						break;
					}
					else if(anno.annotationType() == FormParam.class)
					{
						FormParam annoPraram = (FormParam) anno;
						annoName = annoPraram.value();
						break;
					}
				}
					
				
				String[] requestValue = requestParam.get(annoName);
					
				
				if(paramClass == String.class)
				{
					methodInputData[i] = requestValue[0];
				}
				else if(paramClass == String[].class)
				{
					methodInputData[i] = requestValue;
				}
				
				System.out.println("anno === "+annoName);
					
				
				
				
			}
			
			/*
			if(to.jsonClassName != null || to.jsonClassName.length()>0)
			{
				String dataStr = new String(to.data,"utf-8");
				System.out.println("data json --> "+dataStr);
				Class _class1 = Class.forName(to.jsonClassName);
				Object obj = mapper.readValue(to.data, _class1);
				System.out.println("obj json --> "+obj);
			}
			*/
			
			//method.getParameterAnnotations()
			
			Object obj = _class.newInstance();
			Object returnObj = method.invoke(obj, methodInputData);
			
			log.info("execute method --- > "+returnObj);
			
			to.data = ((String)returnObj).getBytes();
			to.requestParams = null;
			
			byte[] bytes = ObjectUtil.toJason(to);
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			session.getAsyncRemote().sendBinary(buffer);
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;

	}
	
	/*
	@OnMessage
	//public void onBytes(byte[] b, boolean last, Session session) 
	public void onBytes(ByteBuffer bytes, Session session) 
	{
		
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			WSTO to = mapper.readValue(bytes.array(),  WSTO.class);
			
			log.info(" appid --- >"+to.appId);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		} 
		
	}
	*/

	@OnClose
	public void onClose(javax.websocket.Session session, CloseReason closeReason)
	{
		log.info(String.format("Session %s close because of %s", session, closeReason));
		latch.countDown();
	}

	
	public void run() 
	{
		latch = new CountDownLatch(1);

	    ClientManager client = ClientManager.createClient();
	    try 
	    {
	        //client.connectToServer(BaltoroWSClient.class, new URI("ws://localhost:8080/baltoro/ws?appid="+appId));
	    	client.connectToServer(BaltoroWSClient.class, new URI("ws://"+this.appId+".baltoro.io/baltoro/ws"));
	        latch.await();
	    } 
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	    }
	}
}

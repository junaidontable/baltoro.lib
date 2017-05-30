package io.baltoro.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.apache.derby.tools.sysinfo;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.ep.Param;
import io.baltoro.to.WSTO;
import io.baltoro.util.ObjectUtil;

public class BaltoroByteBufferMessageHandler implements MessageHandler.Whole<ByteBuffer>
{
	
	static Logger log = Logger.getLogger(BaltoroByteBufferMessageHandler.class.getName());
	
	private Session session;
	private String appId;
	
	public BaltoroByteBufferMessageHandler(String appId, Session session)
	{
		this.session = session;
		this.appId = appId;
	}

	@Override
	public void onMessage(ByteBuffer bytesBuffer)
	{
		log.info(" appid --- > text"+appId);
		
		
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
					if(anno.annotationType() == Param.class)
					{
						Param annoPraram = (Param) anno;
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
			
			/*
			if(returnObj instanceof String)
			{
				to.data = ((String)returnObj).getBytes();
			}
			else
			{
				to.data = mapper.writeValueAsBytes(returnObj);
			}
			*/
			
			to.data = mapper.writeValueAsBytes(returnObj);
			
			to.requestParams = null;
			
			byte[] bytes = ObjectUtil.toJason(to);
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			session.getAsyncRemote().sendBinary(buffer);
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
}

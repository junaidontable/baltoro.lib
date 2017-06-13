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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.features.CTX;
import io.baltoro.features.Param;
import io.baltoro.to.RequestContext;
import io.baltoro.to.WSTO;
import io.baltoro.util.ObjectUtil;
import io.baltoro.util.StringUtil;

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
	public void onMessage(ByteBuffer bytesBuffer)
	{
		//log.info(" appid --- > text"+appId);
		
		
		try
		{
			byte[] jsonBytes = bytesBuffer.array();
			
			ObjectMapper mapper = new ObjectMapper();
			WSTO to = mapper.readValue(jsonBytes,  WSTO.class);
			
			
			WebMethod wMethod = WebMethodMap.getInstance().getMethod(to.path);
			
			if(wMethod == null)
			{
				WebFile webFile = getFile(to);
				if(webFile != null)
				{
					to.data = webFile.data;
				}
			}
			else
			{
				Object returnObj = executeMethod(wMethod, to);
				if(returnObj != null)
				{
					to.data = mapper.writeValueAsBytes(returnObj);
				}
			}
			
			
			
			to.requestContext = null;
			
			byte[] bytes = ObjectUtil.toJason(to);
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			session.getAsyncRemote().sendBinary(buffer);
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private WebFile getFile(WSTO to) throws Exception
	{
		String path = to.path;
		String[] tokens = path.split("/");
		for (int i = 0; i < tokens.length; i++)
		{
			int lIndex = path.lastIndexOf('/');
			path = path.substring(0, lIndex);
			String lPath = to.path.substring(lIndex+1);
			WebMethod wMethod = WebMethodMap.getInstance().getMethod(path+"/*");
			if(wMethod != null)
			{
				System.out.println(wMethod);
				
				WebFile webFile = Baltoro.fileServer(lPath, wMethod.localFilePath);
				if(webFile == null)
				{
					return null;
				}
				return webFile;
				
			}
			
		}
		
		return null;
	}
	
	
	private Object executeMethod(WebMethod wMethod, WSTO to) throws Exception
	{
		RequestContext ctx = to.requestContext;
		
		if(StringUtil.isNotNullAndNotEmpty(ctx.sessionId))
		{
			UserSession userSession = new UserSession(ctx.sessionId);
		}
		
		Map<String, String[]> requestParam = ctx.requestParams;
		if(requestParam == null || requestParam.size()==0)
		{
			requestParam = new HashMap<String, String[]>();
		}
		
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
			else if(paramClass == RequestContext.class)
			{
				methodInputData[i] = ctx;
			}
			
		}
		

		
		Object obj = _class.newInstance();
		
		
		Object returnObj = method.invoke(obj, methodInputData);
		
		return returnObj;

	}
}

package io.baltoro.client;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.websocket.MessageHandler;
import javax.websocket.Session;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.client.util.ObjectUtil;
import io.baltoro.client.util.StringUtil;
import io.baltoro.exception.AuthException;
import io.baltoro.features.Param;
import io.baltoro.to.RequestContext;
import io.baltoro.to.ResponseContext;
import io.baltoro.to.WSTO;

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
		
		
			byte[] jsonBytes = bytesBuffer.array();
			
			ObjectMapper mapper = new ObjectMapper();
			
			
			WSTO to = null;
			try
			{
				to = mapper.readValue(jsonBytes,  WSTO.class);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				return;
			}
			
			RequestContext req = to.requestContext;
			
			WebMethod wm = WebMethodMap.getInstance().getMethod(req.getApiPath());
			if(wm == null)
			{
				String path = req.getApiPath();
				String[] tokens = path.split("/");
				for (int i = 0; i < tokens.length; i++)
				{
					int lIndex = path.lastIndexOf('/');
					path = path.substring(0, lIndex);
					String lPath = req.getApiPath().substring(lIndex+1);
					wm = WebMethodMap.getInstance().getMethod(path+"/*");
					if(wm != null)
					{
						req.setRelativePath(lPath);
						break;
					}
					
				}
			}
			
			
			
			ResponseContext res = new ResponseContext();
			to.responseContext = res;
			
			if(wm == null)
			{
				res.setError("API for path ["+req.getApiPath()+"] not found ");
			}
			
			try
			{
	
					checkAuth(wm,to, wm.getWebPath());
					Object returnObj = executeMethod(wm, to);
					if(returnObj != null)
					{
						if(returnObj instanceof byte[])
						{
							to.responseContext.setData((byte[]) returnObj);
						}
						else
						{
							to.responseContext.setData(mapper.writeValueAsBytes(returnObj));
						}
					}
				
			} 
			catch(Exception e)
			{
				if(e.getCause() instanceof SendRedirect)
				{
					SendRedirect sd = (SendRedirect) e.getCause();
					res.setRedirect(sd.getUrl());
				}
				
				if(e instanceof AuthException)
				{
					res.setError(e.getMessage());
				}
				
			}
			
			
			try
			{
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
	
	
	private void checkAuth(WebMethod wm, WSTO to, String path)
	throws AuthException
	{
		if(!wm.authRequired)
		{
			return;
		}
		
		String sessionId = to.requestContext.getSessionId();
		if(sessionId == null)
		{
			throw new AuthException("sessionId is null, cannot execute "+path);
		}
		
		UserSession userSession = Baltoro.getUserSession(sessionId);
		if(userSession == null)
		{
			throw new AuthException("session object is null, cannot execute "+path);
		}
		
		Principal principal = userSession.getPrincipal();
		if(principal == null)
		{
			throw new AuthException("no user in session, cannot execute "+path);
		}
	}
	
	private Object executeMethod(WebMethod wMethod, WSTO to) throws Exception
	{
		RequestContext ctx = to.requestContext;
		
		if(StringUtil.isNotNullAndNotEmpty(ctx.getSessionId()))
		{
			UserSession userSession = new UserSession(ctx.getSessionId());
		}
		
		Map<String, String[]> requestParam = ctx.getRequestParams();
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

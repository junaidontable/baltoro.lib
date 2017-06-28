package io.baltoro.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.client.util.ObjectUtil;
import io.baltoro.client.util.StringUtil;
import io.baltoro.exception.AuthException;
import io.baltoro.features.Param;
import io.baltoro.to.RequestContext;
import io.baltoro.to.ResponseContext;
import io.baltoro.to.UserSessionContext;
import io.baltoro.to.WSTO;

public class RequestWorker extends Thread
{
	private ByteBuffer byteBuffer;
	static ObjectMapper mapper = new ObjectMapper();
	
	static ThreadLocal<RequestContext> requestCtx = new ThreadLocal<>();
	//static ThreadLocal<ResponseContext> responseCtx = new ThreadLocal<>();

	public RequestWorker(ByteBuffer byteBuffer)
	{
		this.byteBuffer = byteBuffer;
	}

	public void run()
	{

		
		try
		{
			
			WSTO to = process();
			
			
			System.out.println("......... ********  ......... processibng byte buffer "+to.appName);
			
			byte[] bytes = ObjectUtil.toJason(to);
			ByteBuffer buffer = ByteBuffer.wrap(bytes);

			WSSessions.get().addToResponseQueue(buffer);
			
			requestCtx.set(null);

		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String sync = "response-queue";
		synchronized (sync.intern())
		{
			sync.intern().notify();
		}

	

	}

	private WSTO process()
	{

		byte[] jsonBytes = byteBuffer.array();

		

		WSTO to = null;
		try
		{
			to = mapper.readValue(jsonBytes, WSTO.class);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		RequestContext req = to.requestContext;
		
		requestCtx.set(to.requestContext);
		

		WebMethod wm = WebMethodMap.getInstance().getMethod(req.getApiPath());
		if (wm == null)
		{
			String path = req.getApiPath();
			String[] tokens = path.split("/");
			for (int i = 0; i < tokens.length; i++)
			{
				int lIndex = path.lastIndexOf('/');
				path = path.substring(0, lIndex);
				String lPath = req.getApiPath().substring(lIndex + 1);
				wm = WebMethodMap.getInstance().getMethod(path + "/*");
				if (wm != null)
				{
					req.setRelativePath(lPath);
					break;
				}

			}
		}

		ResponseContext res = new ResponseContext();
		to.responseContext = res;

		if (wm == null)
		{
			res.setError("API for path [" + req.getApiPath() + "] not found ");
		}

		try
		{

			checkAuth(wm, to, wm.getWebPath());
			Object returnObj = executeMethod(wm, to);
			if (returnObj != null)
			{
				if (returnObj instanceof byte[])
				{
					to.responseContext.setData((byte[]) returnObj);
				} 
				else
				{
					to.responseContext.setData(mapper.writeValueAsBytes(returnObj));
				}
			}

		} catch (Exception e)
		{
			if (e.getCause() instanceof SendRedirect)
			{
				SendRedirect sd = (SendRedirect) e.getCause();
				res.setRedirect(sd.getUrl());
			} else if (e instanceof AuthException)
			{
				res.setError(e.getMessage());
			} else
			{
				e.printStackTrace();
			}

		}

		to.requestContext = null;
		
		return to;

	}

	private void checkAuth(WebMethod wm, WSTO to, String path) throws AuthException
	{
		if (!wm.authRequired)
		{
			return;
		}

		String sessionId = to.requestContext.getSessionId();
		if (sessionId == null)
		{
			throw new AuthException("sessionId is null, cannot execute " + path);
		}

		UserSession userSession = Baltoro.getUserSession();
		if (userSession == null)
		{
			throw new AuthException("session object is null, cannot execute " + path);
		}

		String userName = userSession.getUserName();
		if (userName == null)
		{
			throw new AuthException("no user in session, cannot execute " + path);
		}
	}

	private Object executeMethod(WebMethod wMethod, WSTO to) throws Exception
	{
		RequestContext ctx = to.requestContext;
		
		
		
		UserSession userSession = null;
		if (StringUtil.isNotNullAndNotEmpty(ctx.getSessionId()))
		{
			userSession = SessionManager.getSession(ctx.getSessionId());
			UserSessionContext uctx = to.userSessionContext;
			if(uctx != null)
			{
				String userName = uctx.getPrincipalName();
				userSession.userName = userName;
				
				Map<String, String> attMap = mapper.readValue(uctx.getAttJson(), Map.class);
				for (String key : attMap.keySet())
				{
					String val = attMap.get(key);
					userSession.attMap.put(key, val);
				}
			}
			
			
		}
		
		
		Map<String, String[]> requestParam = ctx.getRequestParams();
		if (requestParam == null || requestParam.size() == 0)
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
				if (anno.annotationType() == Param.class)
				{
					Param annoPraram = (Param) anno;
					annoName = annoPraram.value();
					break;
				}

			}

			String[] requestValue = requestParam.get(annoName);

			if (paramClass == String.class)
			{
				methodInputData[i] = requestValue[0];
			} else if (paramClass == String[].class)
			{
				methodInputData[i] = requestValue;
			} else if (paramClass == RequestContext.class)
			{
				methodInputData[i] = ctx;
			} else if (paramClass == UserSession.class)
			{
				methodInputData[i] = userSession;
			}

		}

		Object obj = _class.newInstance();
		
		
		Object returnObj = method.invoke(obj, methodInputData);

		return returnObj;

	}
}
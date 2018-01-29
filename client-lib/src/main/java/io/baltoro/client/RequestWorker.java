package io.baltoro.client;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.to.APIError;
import io.baltoro.to.RequestContext;
import io.baltoro.to.ResponseContext;
import io.baltoro.to.UserSessionContext;
import io.baltoro.to.WSTO;
import io.baltoro.client.util.ObjectUtil;
import io.baltoro.client.util.StringUtil;
import io.baltoro.exp.AuthException;
import io.baltoro.features.AbstractFilter;
import io.baltoro.features.Param;

public class RequestWorker extends Thread
{
	private ByteBuffer byteBuffer;
	UserSession userSession;
	List<AbstractFilter> filters = new ArrayList<>();
	static ObjectMapper mapper = new ObjectMapper();
	
	static ThreadLocal<RequestContext> requestCtx = new ThreadLocal<>();
	static ThreadLocal<ResponseContext> responseCtx = new ThreadLocal<>();

	public RequestWorker(ByteBuffer byteBuffer)
	{
		this.byteBuffer = byteBuffer;
	}

	public void run()
	{

		WSTO to = getWSTO();
		if(to == null)
		{
			System.out.println("ERROR PARSING WSTO !!!!!!! CHECK ");
			return;
		}
		
		
		RequestContext req = to.requestContext;
		requestCtx.set(req);
		
		ResponseContext res = new ResponseContext();
		to.responseContext = res;
		res.setSessionId(req.getSessionId());
		
		responseCtx.set(res);
	
		
		try
		{
			
			process(to);
		
		} 
		catch (Exception e)
		{
			if (e instanceof SendRedirect)
			{
				SendRedirect sd = (SendRedirect) e;
				res.setRedirect(sd.getUrl());
			}
			else if (e.getCause() instanceof SendRedirect)
			{
				SendRedirect sd = (SendRedirect) e.getCause();
				res.setRedirect(sd.getUrl());
			} 
			else if (e instanceof APIError)
			{
				APIError er = (APIError) e;
				res.setError(er.getMessage());
			}
			else if (e.getCause() instanceof APIError)
			{
				APIError er = (APIError) e.getCause();
				res.setRedirect(er.getMessage());
			} 
			else if (e instanceof AuthException)
			{
				res.setError(e.getMessage());
			}
			else
			{
				e.printStackTrace();
				res.setError(e.getMessage()+"---"+(e.getCause() !=null ? e.getCause().getMessage() : ""));
				
			}

		}
		finally 
		{
			to.requestContext = null;
			byte[] bytes = null;
			try
			{
				bytes = ObjectUtil.toJason(to);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("CANNOT CONVERT TO JSON , !!!! CHECK !");
				return;
			}
			ByteBuffer buffer = ByteBuffer.wrap(bytes);

			WSSessions.get().addToResponseQueue(buffer);
			
			requestCtx.set(null);
			
		
		}

		String sync = "response-queue";
		synchronized (sync.intern())
		{
			sync.intern().notify();
		}

	

	}
	
	
	private WSTO getWSTO()
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

		
		return to;
	}
	


	private void process(WSTO to) throws Exception
	{

		RequestContext req = to.requestContext;
		ResponseContext res = to.responseContext;
		
		if (StringUtil.isNotNullAndNotEmpty(req.getSessionId()))
		{
			String reqSessionId = req.getSessionId();
			userSession = SessionManager.getSession(reqSessionId);
			UserSessionContext uctx = to.userSessionContext;
			if(uctx != null)
			{
				String userName = uctx.getPrincipalName();
				userSession.userName = userName;
				
				Map<String, String> attMap = new HashMap<>();
				try
				{
					if(uctx.getAttJson() != null)
					{
						attMap = mapper.readValue(uctx.getAttJson(), Map.class);
					}
					
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
				
				for (String key : attMap.keySet())
				{
					String val = attMap.get(key);
					userSession.attMap.put(key, val);
				}
			}
			
			
		}
		
		
		
		List<String> filterNames = WebMethodMap.getInstance().getFilterNames();
		for (String fNames : filterNames)
		{
			Class<AbstractFilter> _class = WebMethodMap.getInstance().getFilterClass(fNames);
			try
			{
				System.out.println(" filter  >>>>>>> "+fNames);
				AbstractFilter filter = _class.newInstance();
				filters.add(filter);
				filter.before(to, userSession);
			} 
			catch (Exception e)
			{
				if (e instanceof SendRedirect)
				{
					SendRedirect sd = (SendRedirect) e;
					res.setRedirect(sd.getUrl());
					return;
				} 
				else if (e.getCause() instanceof SendRedirect)
				{
					SendRedirect sd = (SendRedirect) e.getCause();
					res.setRedirect(sd.getUrl());
					return;
				} 
				else if (e instanceof AuthException)
				{
					res.setError(e.getMessage());
					return;
				} 
				else
				{
					e.printStackTrace();
				}

			}
		}
		
		

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

		

		if (wm == null)
		{
			res.setError("API for path [" + req.getApiPath() + "] not found ");
		}

		try
		{

			checkAuth(wm, to, wm.getWebPath());
			Object returnObj = executeMethod(wm, to);
			
			for (AbstractFilter filter : filters)
			{
				filter.after(returnObj, to, userSession);
			}
			
			
			if (returnObj != null)
			{
				if(returnObj instanceof String)
				{
					to.responseContext.setData(((String) returnObj).getBytes());
				}
				else if (returnObj instanceof byte[])
				{
					to.responseContext.setData((byte[]) returnObj);
				} 
				else
				{
					to.responseContext.setData(mapper.writeValueAsBytes(returnObj));
				}
			}

		} 
		catch (Exception e)
		{
			throw e;
		}


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
			throw new AuthException("no auth user in session, cannot execute " + path);
		}
	}

	private Object executeMethod(WebMethod wMethod, WSTO to) throws Exception
	{
		RequestContext ctx = to.requestContext;
		
		
		
		String path = ctx.getApiPath();
		
		
		Map<String, String[]> requestParam = ctx.getRequestParams();
		if (requestParam == null || requestParam.size() == 0)
		{
			requestParam = new HashMap<String, String[]>();
		}

		Class<?> _class = wMethod.get_class();
		Method method = wMethod.getMethod();

		//boolean noParam = true;
		Parameter[] methodParms = method.getParameters();
		Object[] methodInputData = new Object[methodParms.length];

		for (int i = 0; i < methodParms.length; i++)
		{
			//noParam = true;
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
			if(annoName != null && requestValue == null)
			{
				StringBuffer buffer = new StringBuffer();
				for (String paramName : requestParam.keySet())
				{
					buffer.append(paramName+",");
				}
				throw new Exception(annoName+" is not submitted as a parameter. incoming params ["+buffer.toString()+"] ");
			}

			if (paramClass == String.class)
			{
				methodInputData[i] = requestValue[0];
			} 
			else if (paramClass == String[].class)
			{
				methodInputData[i] = requestValue;
			} 
			else if (paramClass == RequestContext.class)
			{
				methodInputData[i] = ctx;
			} 
			else if (paramClass == UserSession.class)
			{
				methodInputData[i] = userSession;
			}

		}

		Object obj = _class.newInstance();
		
		
		Object returnObj = method.invoke(obj, methodInputData);

		return returnObj;

	}
}
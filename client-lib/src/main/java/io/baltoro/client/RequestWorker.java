package io.baltoro.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.client.util.StringUtil;
import io.baltoro.exp.AuthException;
import io.baltoro.features.AbstractFilter;
import io.baltoro.features.Param;
import io.baltoro.to.APIError;
import io.baltoro.to.ContentTO;
import io.baltoro.to.RequestContext;
import io.baltoro.to.ResponseContext;
import io.baltoro.to.SessionUserTO;
import io.baltoro.to.WSTO;

public class RequestWorker extends Thread
{
	private WSTO to;
	UserSession userSession;
	List<AbstractFilter> filters = new ArrayList<>();
	static ObjectMapper mapper = new ObjectMapper();
	boolean run = true;
	static int _count;
	int count;
	long lastWorked = System.currentTimeMillis();
	

	
	RequestWorker()
	{
	
	}
	
	void set(WSTO to)
	{
		//System.out.println(this+" ........... on set  : "+to.requestContext.getApiPath());
		this.to = to;
		synchronized (this)
		{
			this.notify();
		}
	}
	
	void clear()
	{
		this.to = null;
		this.userSession = null;
		filters.clear();
	}
	
	public void run()
	{
		while (run)
		{
			if(to == null)
			{
				synchronized (this)
				{
					try
					{
						this.wait(10000);
						
						if(to == null)
						{
							//System.out.println("REQUEST thread no work to do  "+this+",  --- "+count+",,,"+WorkerPool.info());
						
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
				//String url = to.requestContext.getApiPath();
				//System.out.println(this+" 1........... on work  : "+url);
				work();
				//System.out.println(this+" 2........... on work  : "+url);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally 
			{
				lastWorked = System.currentTimeMillis();
				to = null;
				WorkerPool.done(this);
			}
			
		}
	}
	
	private void work()
	{

		if(to == null)
		{
			System.out.println("ERROR PARSING WSTO !!!!!!! CHECK ");
			return;
		}
		
		RequestContext req = to.requestContext;
		//requestCtx.set(req);
		
		ResponseContext res = new ResponseContext();
		res.setHeaders(new HashMap<>());
		to.responseContext = res;
		res.setSessionId(req.getSessionId());
		
		Baltoro.userSessionIdCtx.set(req.getSessionId());
		Baltoro.serviceNameCtx.set(to.serviceName);
		Baltoro.userRequestCtx.set(req);
		Baltoro.userResponseCtx.set(res);
		
		UserSession userSession = SessionManager.getSession(req.getSessionId());
		for (String ck : req.getCookies().keySet())
		{
			String v = req.getCookies().get(ck);
			userSession.addCookie(ck, v);
		}
		
		//responseCtx.set(res);
		
		try
		{
			
			process();
		
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
			
			
			ResponseQueue.instance().addToResponseQueue(to);
			
			Baltoro.userSessionIdCtx.set(null);
			Baltoro.serviceNameCtx.set(null);
			Baltoro.userRequestCtx.set(null);
			Baltoro.userResponseCtx.set(null);
			
		
		}

	}
	
	

	private void process() throws Exception
	{

		RequestContext req = to.requestContext;
		ResponseContext res = to.responseContext;
		
		
		if (StringUtil.isNotNullAndNotEmpty(req.getSessionId()))
		{
			String reqSessionId = req.getSessionId();
			
			userSession = SessionManager.getSession(reqSessionId);
			if(!userSession.isAuthenticated())
			{
				/*
				long t0 = System.currentTimeMillis();
				long tc = userSession.getCreatedOn();
				long dif = t0-tc;
				if(dif > 59000)
				*/
				if(userSession.synced == false)
				{
					userSession.synced = true;
					SessionUserTO to = Baltoro.cs.pullSession(reqSessionId);
					if(to != null)
					{
						userSession = SessionManager.getSession(reqSessionId);
						userSession.setRoles(to.roles);
						userSession.attMap = to.att;
						userSession.setUserName(to.userName);
						userSession.setAuthenticated(to.authenticated);
					}
				}
				
			}
			
		}
		
		
		
		List<String> filterNames = WebMethodMap.getInstance().getFilterNames();
		for (String fNames : filterNames)
		{
			Class<AbstractFilter> _class = WebMethodMap.getInstance().getFilterClass(fNames);
			try
			{
				//System.out.println(" filter  >>>>>>> "+fNames);
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
		
		
		String url = req.getApiPath();
		if(url.equals("/"))
		{
			url = "/app_root";
		}	
		else if(url.lastIndexOf('/') == 0)
		{
			url = "/app_root"+url;
		}
		
		if(url.endsWith("/"))
		{
			url = url.substring(0, url.length()-1);
		}
		
		
		WebMethod wm = WebMethodMap.getInstance().getMethod(url);
		if (wm == null)
		{
			String path = req.getApiPath();
			String[] tokens = path.split("/");
			for (int i = 0; i < tokens.length; i++)
			{
				int lIndex = path.lastIndexOf('/');
				if(lIndex == -1)
				{
					System.out.println("no index found / error path="+path);
				}
				
				try
				{
					path = path.substring(0, lIndex);
				} 
				catch (StringIndexOutOfBoundsException e)
				{
					String error = "Can't find API path["+url+", "+path+"] on hostid="+Baltoro.hostId+", serviceNames="+Baltoro.serviceNames.toString();
					System.out.println(error);
					e.printStackTrace();
					throw new Exception(error);
				}
				
				
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
			

			//System.out.println(")))) >>>>>>>>>>>>>>>> executing "+to.requestContext.getApiPath()+" return : "+returnObj);
		
			
			if (returnObj != null)
			{
			
				
				if(returnObj instanceof String)
				{
					to.responseContext.setData(((String) returnObj).getBytes());
				}
				else if (returnObj instanceof byte[])
				{
					to.responseContext.hasBinaryData = true;
					to.responseContext.setData((byte[]) returnObj);
				} 
				else
				{
					//byte[] bytes = ObjectUtil.convertToBytes(returnObj);
					//to.responseContext.setData(bytes);
					//String json = mapper.writeValueAsString(returnObj);
					//to.responseContext.setData(mapper.writeValueAsBytes(returnObj));
					to.responseContext.setData(mapper.writeValueAsBytes(returnObj));
				}
			}
			else
			{
				to.responseContext.noReturnData = true;
			}

		} 
		catch (Exception e)
		{
			e.printStackTrace();
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
			throw new AuthException("Authentiction required for " + path);
		}

		UserSession userSession = Baltoro.getUserSession();
		if (userSession == null)
		{
			throw new AuthException("Authentiction required for " + path);
		}

		if (!userSession.isAuthenticated())
		{
			throw new AuthException("Authentiction required for " + path);
		}
	}

	private Object executeMethod(WebMethod wMethod, WSTO to) throws Exception
	{
		RequestContext reqCtx = to.requestContext;
		ResponseContext resCtx = to.responseContext;
		/*
		WebSocketContext wsCtx = to.webSocketContext;
		WSSession wssession = null;
		if(wsCtx != null)
		{
			wssession = (WSSession) WSAPIClassInstance.get().get(wsCtx.getInitRequestUuid(), WSSession.class);
			if(wssession == null)
			{
				wssession = new WSSession(to);
			}
		}
		*/
		
		Map<String, String[]> requestParam = reqCtx == null ? null : reqCtx.getRequestParams();
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
			if(annoName != null && requestValue == null && paramClass != Optional.class)
			{
				StringBuffer buffer = new StringBuffer();
				for (String paramName : requestParam.keySet())
				{
					buffer.append(paramName+",");
				}
				throw new Exception("["+to.requestContext.getApiPath()+"]" + annoName+" is not submitted as a parameter. incoming params ["+buffer.toString()+"] ");
			}

			
			
			
	
			
			if(annoName != null)
			{
				if (paramClass == String.class && requestValue != null)
				{
					methodInputData[i] = requestValue[0];
				} 
				else if (paramClass == Optional.class)
				{
					if(requestValue == null)
					{
						methodInputData[i] = null;
					}
					else
					{
						methodInputData[i] = Optional.of(requestValue[0]);
					}
				} 
				else if (paramClass == String[].class && requestValue != null)
				{
					methodInputData[i] = requestValue;
				} 
				else if (requestValue != null && !paramClass.isArray() && isContentClass(paramClass))
				{
					if(requestValue.length > 1)
					{
						new Exception("File upload, has multiple files coming in. use UploadedFile[] or fix the UI to receive one file .");
					}
					
					ContentTO cto = mapper.readValue(requestValue[0], ContentTO.class);
					
					Content ct = (Content) paramClass.newInstance();
					ct.setServerUuid(cto.uuid);
					ct.setName(cto.fileName);
					ct.setSize(cto.size);
					ct.setContentType(cto.type);
					
					methodInputData[i] = ct;
				} 
				else if (requestValue != null && paramClass.isArray() && isContentClass(paramClass))
				{
					String[] filesJson = requestValue;
					        
					Content[] files = (Content[]) Array.newInstance(paramClass.getComponentType(), filesJson.length);
					int cnt = 0;
					for (String json : filesJson)
					{
						ContentTO cto = mapper.readValue(json, ContentTO.class);
						files[cnt] = (Content) paramClass.getComponentType().newInstance();
						files[cnt].setServerUuid(cto.uuid);
						files[cnt].setName(cto.fileName);
						files[cnt].setSize(cto.size);
						files[cnt].setContentType(cto.type);
						cnt++;
					}
					
					
					methodInputData[i] = files;
				} 
				else
				{
					methodInputData[i] = mapper.readValue(requestValue[0], paramClass);
				}
			}
			else
			{
				if (paramClass == RequestContext.class)
				{
					methodInputData[i] = reqCtx;
				} 
				else if (paramClass == ResponseContext.class)
				{
					methodInputData[i] = resCtx;
				}
				else if (paramClass == UserSession.class)
				{
					methodInputData[i] = userSession;
				}
				/*
				else if (paramClass == WebSocketContext.class)
				{
					methodInputData[i] = wsCtx;
				}
				else if (paramClass == WSSession.class)
				{
					methodInputData[i] = wssession;
				}
				*/
			}
			
			/*
			if (paramClass == byte[].class && wsCtx != null && wsCtx.getApiPath().endsWith("onmessage"))
			{
				methodInputData[i] = wsCtx.getData();
			}
			
			if (paramClass == String.class && wsCtx != null && wsCtx.getApiPath().endsWith("onmessage"))
			{
				methodInputData[i] = wsCtx.getMessage();
			}
			*/
		}
		

		Object  classInstance = null;
		
		if(wMethod.isWebSocket())
		{
			/*
			if(method.isAnnotationPresent(OnOpen.class))
			{
				classInstance = _class.newInstance();
				WSAPIClassInstance.get().add(wsCtx.getInitRequestUuid(),_class, classInstance);
				WSAPIClassInstance.get().add(wsCtx.getInitRequestUuid(),WSSession.class, wssession);
				WSAPIClassInstance.get().add(wsCtx.getInitRequestUuid(),WSTO.class, to);
				
				System.out.println("classInstance cache OnOPen "+wsCtx.getInitRequestUuid()+" - "+_class+" - "+wsCtx.getWsSessionUuid());
			}
			
			classInstance = WSAPIClassInstance.get().get(wsCtx.getInitRequestUuid(), _class);
			
			
			
			
			if(method.isAnnotationPresent(OnClose.class))
			{
				WSAPIClassInstance.get().remove(wsCtx.getInitRequestUuid(), _class);
				WSAPIClassInstance.get().remove(wsCtx.getInitRequestUuid(),WSSession.class);
				WSAPIClassInstance.get().remove(wsCtx.getInitRequestUuid(),WSTO.class);
			}
			*/
		}
		else
		{
			classInstance = _class.newInstance();
		}
		
		if(classInstance == null)
		{
			System.out.println("classInstance is null why ?  - "+_class);
			return null;
		}
	
			
		Object returnObj = method.invoke(classInstance, methodInputData);

	
			
		return returnObj;

	}
	
	
	private boolean isContentClass(Class _class)
	{
		Class nClass = _class;
		
		boolean isArray = nClass.isArray();
		if(isArray)
		{
			nClass = _class.getComponentType();
		}
		
		if(nClass == Content.class)
		{
			return true;
		}
		
		for (int i = 0; i < 10; i++)
		{
			Class pClass = nClass.getSuperclass();
			if(pClass == null)
			{
				return false;
			}
			
			if(pClass == Content.class)
			{
				return true;
			}
			
			nClass = pClass;
		}
		
		return false;
		
	}
}
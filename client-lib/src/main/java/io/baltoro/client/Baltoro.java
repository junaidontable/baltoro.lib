package io.baltoro.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.ws.rs.core.NewCookie;

import com.google.common.net.InetAddresses;

import io.baltoro.client.util.StringUtil;
import io.baltoro.ep.ClassBuilder;
import io.baltoro.ep.CloudServer;
import io.baltoro.ep.EPData;
import io.baltoro.ep.ParamInput;
import io.baltoro.to.RequestContext;
import io.baltoro.to.ResponseContext;


public class Baltoro 
{
	
	static Logger log = Logger.getLogger(Baltoro.class.getName());
	
	static
	{
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT:%4$s > %5$s%6$s%n");
	}
	
	static ThreadLocal<String> userSessionIdCtx = new ThreadLocal<>();
	static ThreadLocal<RequestContext> userRequestCtx = new ThreadLocal<>();
	static ThreadLocal<ResponseContext> userResponseCtx = new ThreadLocal<>();
	
	static ThreadLocal<String> serviceNameCtx = new ThreadLocal<>();
	static Map<String, Class<?>> pathClassMap = new HashMap<String, Class<?>>(100); 
	static Map<String, NewCookie> cookieMap = new HashMap<String, NewCookie>(100);

	static List<ServicePackage> serviceList = new ArrayList<ServicePackage>();
	
	static StringBuffer serviceNames = new StringBuffer();
	static String hostId;
	static APIClient cs;
	
	
	static String instanceUuid;
	static int instanceThreadCount = 3;
	static Properties props = null;
	static String appUuid;
	static String appPrivateKey;
	static String appName;
	static String userUuid;
	static File propFile;
	static String serverURL = "http://"+APIClient.BLTC_CLIENT+".baltoro.io";
	static String appURL;
	static Env env = Env.PRD;
	
	
	static String pullReplicationServiceNames;
	
	static ResponsePoller responsePoller;
	
	private static boolean running = false;
	static int dbConnectionPoolSize = 10;
	
	
	static LocalDB db;
	

	
	private static void buildService() throws Exception
	{
			
		Map<String, WebMethod> pathMap = new HashMap<String, WebMethod>(200);
		
		AnnotationProcessor p = new AnnotationProcessor();
		
		for (ServicePackage sp : serviceList)
		{
			
			for (String _package : sp.packageNames)
			{
				//Map<String, WebMethod> pMap = p.processAnnotation(_package.trim());
				Map<String, WebMethod> pMap = p.processAnnotation(sp.serviceName, _package);
				pathMap.putAll(pMap);
			}
			
			WebMethodMap.getInstance().setMap(pathMap);
			
		}
	}
	
	
	public static LocalDB getDB()
	{
		if(!Baltoro.running)
		{
			System.out.println("Baltoro not running, first call Baltoro.start() method ... ");
			System.out.println("Shutting down ... ");
			System.exit(1);
		}
		
		
		return LocalDB.instance();
	}
	
	public static void setDBConnectionPoolSize(int size)
	{
		Baltoro.dbConnectionPoolSize = size;
	}
	
	public static String getMainClassName() 
	{ 
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        StackTraceElement ste = stElements[stElements.length-1];
    
   
        return ste.getClassName();
    }
	
	public static Env getEnv()
	{
		return env;
	}
	
	public static String getMainClassPackageName() 
	{ 
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        StackTraceElement ste = stElements[stElements.length-1];
    
        String className = ste.getClassName();
        System.out.println(" ---- > "+className);
        
       // ste.getClass().getClassLoader().
        
//        Package _pack = ste.getClass().getPackage();
//        if(_pack == null)
//        {
//        	return null;
//        }
      //  _pack.
        
        /*
        Package[] packages = Package.getPackages();
        for (Package pack : packages)
		{
        	String vendor = pack.getImplementationVendor();
        	if(vendor != null)
        	{
        		continue;
        	}
        	
			System.out.println(pack+"-"+pack.getImplementationVendor());
		}
        
        return packages[0].getName();
        */
        
        //String pack = _pack.getName();
        
        String[] packs = className.split("\\.");
        if(packs.length > 1)
        {
        	return packs[0]+"."+packs[1];
        }
        else
        {
        	return packs[0];
        }
     }
	
	
	public static <T> T endPointFactory(Class<T> _class)
	{
		try
		{
			Class<?> implClass = pathClassMap.get(_class.getName());
			if(implClass == null)
			{
				ClassBuilder builder = new ClassBuilder(_class);
				implClass = builder.buildClass();
				pathClassMap.put(_class.getName(), implClass);
			}
			
			Object obj = implClass.newInstance();
			return _class.cast(obj);
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static <T> T callSync(String path, Class<T> returnType, ParamInput input)
	{
		return callSync(Baltoro.appName, Baltoro.userRequestCtx.get(), path, returnType, input);
	}
	
	public static <T> T callSync(String path, Class<T> returnType)
	{
		return callSync(Baltoro.appName, Baltoro.userRequestCtx.get(), path, returnType, null);
	}
	
	private static <T> T callSync(String appName, RequestContext req, String path, Class<T> returnType, ParamInput input)
	{
		try
		{
			CloudServer cServer = new CloudServer(appName, req);
			EPData epData = null;
			if(input != null)
			{
				epData = input.getEPData();
			}
			
			
			String url = userRequestCtx.get().getUrl();
			int idx1 = url.indexOf("://");
			int idx2 = url.indexOf("/", idx1+3);
			
			String serverUrl = url.substring(0, idx2);
			
			T t = cServer.call(serverUrl, path, epData, returnType);
			return t;
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	public static Future<?> callAsync(String path, Class<?> returnType, ParamInput input)
	{
		return callAsync(appName, Baltoro.userRequestCtx.get(), path, returnType, input);
	}
	
	public static Future<?> callAsync(String path, Class<?> returnType)
	{
		return callAsync(appName, Baltoro.userRequestCtx.get(), path, returnType, null);
	}
	
	public static Future<?> callAsync(String appName, RequestContext req, String path, Class<?> returnType, ParamInput input)
	{
		try
		{
			CloudServer cServer = new CloudServer(appName, req);
			EPData epData = null;
			if(input != null)
			{
				epData = input.getEPData();
			}
				
			Future<?> f = cServer.callAsyn(path, epData, returnType);
			
			return f;
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	public static void validateSession(String userName, Set<String> roleNames, int sessionTimeoutMin)
	{
		String userSessionId = userSessionIdCtx.get();
		if(userSessionId == null)
		{
			return;
		}
		
		UserSession userSession = SessionManager.getSession(userSessionId);
		userSession.setUserName(userName);
		userSession.setAuthenticated(true);
		userSession.setRoles(roleNames);
		userSession.setTimeoutMin(sessionTimeoutMin);
		userSession.sendSession();
		
	}
	
	
	public static UserSession getUserSession()
	{
		//RequestContext rc = RequestWorker.requestCtx.get();
		String userSessionId = userSessionIdCtx.get();
		if(userSessionId == null)
		{
			return null;
		}
		UserSession userSession = SessionManager.getSession(userSessionId);
		
		return userSession;
	}
	
	public static void invalidateSession()
	{
		//RequestContext rc = RequestWorker.requestCtx.get();
		
		String userSessionId = userSessionIdCtx.get();
		if(userSessionId == null)
		{
			return;
		}
		
		UserSession userSession = SessionManager.getSession(userSessionId);
		userSession.setUserName(null);
		userSession.setAuthenticated(false);
		userSession.setRoles(null);;
		userSession.attMap = null;
		
		SessionManager.removeSession(userSessionId);
		userSession.sendSession();
	}
	
	public static void init(String appName)
	{
		init(appName, null);
	}
	
	public static String getServerUrl()
	{
		return serverURL;
	}
	
	public static void init(String appName, Env env)
	{
		
		String url = System.getProperties().getProperty("url");
		if(StringUtil.isNotNullAndNotEmpty(url))
		{
			serverURL = url;
		}
		

		String _envStr = System.getProperties().getProperty("env");
		if(StringUtil.isNotNullAndNotEmpty(_envStr))
		{
			Env _env = null;
			try
			{
				_env = Env.valueOf(_envStr.toUpperCase());
			} 
			catch (Exception e)
			{
				System.out.println("Check JVM argument -Denv=? ALLOWED env values are "+Arrays.toString(Env.values()));
				System.out.println("shut down ...");
				System.exit(1);
			}
			
			
			Baltoro.env = _env;
		}
		else
		{
			Baltoro.env = env;
		}

		if(env == null)
		{
			env = Env.PRD;
		}
		
		Baltoro.appName = appName;
		
		//int hostIdx1 = serverURL.indexOf("://");
		//int hostIdx2 = serverURL.indexOf(hostIdx1,"/");
		
		boolean isIP = InetAddresses.isInetAddress(serverURL);
		
		
		switch (env)
		{
			case PRD:
				Baltoro.appName = appName;
				//serverURL = serverProtocol+"://"+Baltoro.appName+"."+serverDomain+":"+serverPort;	
				
				break;
				
			case STG:
				Baltoro.appName = appName+"-envsg";
				//serverURL = serverProtocol+"://"+Baltoro.appName+"."+serverDomain+":"+serverPort;
				break;
				
			case QA:
				Baltoro.appName = appName+"-envqa";
				//serverURL = serverProtocol+"://"+Baltoro.appName+"."+serverDomain+":"+serverPort;
				break;	
				
			case DEV:
				Baltoro.appName = appName+"-envdv";
				//serverURL = serverProtocol+"://"+Baltoro.appName+"."+serverDomain+":"+serverPort;
				break;	

				
			default :
				//serverURL = serverProtocol+"://www.baltoro.io";
				break;
		}
		
		appURL = serverURL.replace("://"+APIClient.BLTC_CLIENT, "://"+Baltoro.appName);
		
		
		
		
	}
	
	public static void register(String serviceName, String ... packageNames)
	{
		if(StringUtil.isNullOrEmpty(serviceName))
		{
			serviceName = "/";
		}
		
		String[] _packageNames = new String[packageNames.length+1];
		_packageNames[_packageNames.length-1] = "io.baltoro.client.APITest";
		
		for (int i = 0; i < packageNames.length; i++)
		{
			_packageNames[i] = packageNames[i];
		}
		ServicePackage sp = new ServicePackage(serviceName, _packageNames);
		serviceList.add(sp);
		
		serviceNames.append(serviceName+",");
	}
	
	
	/**
	 * useage appName:serviceName:objectType:ObjectUuid, if no appName then serviceName is for default appName
	 * example upcap:deals - upcap is app name, deals is the service name
	 * example deals - deals is the servicename in current app
	 * @param serviceName
	 */
	public static void pullReplication(String ... serviceName)
	{
		for (int i = 0; i < serviceName.length; i++)
		{
			pullReplicationServiceNames = "service:"+serviceName[i] + " ";
		}
		
		pullReplicationServiceNames = pullReplicationServiceNames.substring(0 , pullReplicationServiceNames.length()-1);
		
	}
	
	
	
	public static void start()
	{

		
		try
		{
			processEnv();
			
			buildService();
			
			cs.sendAppAPI();
			
			RequestPoller.instance();
			//requestPoller.start();
			
			
			responsePoller = new ResponsePoller();
			responsePoller.start();
			
			running = true;
			
			for (ServicePackage sp : serviceList)
			{
				log.info("=====================================================");
				log.info("=====================================================");
				if(Baltoro.serverURL.contains("localhost") || Baltoro.serverURL.contains("127.0.0.1"))
				{
					log.info("Test URL --> "+Baltoro.serverURL+"/"+sp.serviceName+"/helloworld?appName="+Baltoro.appName);
				}
				else
				{
					log.info("Test URL --> "+Baltoro.appURL+"/"+sp.serviceName+"/helloworld");
					String hostUrl = Baltoro.appURL.substring(0,appURL.indexOf('.'))+"-hid"+hostId+""+Baltoro.appURL.substring(appURL.indexOf('.'));
					log.info("Host URL --> "+hostUrl+"/"+sp.serviceName+"/helloworld");
				}
				log.info("HOST ID ====> "+hostId);
				log.info("INST UUID ====> "+instanceUuid);
				log.info("=====================================================");
				log.info("=====================================================");
				
			}
			
			db = LocalDB.instance();
			
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	
	}
	

    
    private static void processEnv() throws Exception
    {
    	  
    	Properties hostProps = new Properties();
		String hostPropFileName = System.getProperty("user.home")+"/baltoro_host.env";
		File hostPropFile = new File(hostPropFileName);
		if(!hostPropFile.exists())
		{
			hostPropFile.createNewFile();
		}
		hostProps.load(new FileInputStream(hostPropFile));
		
		hostId = hostProps.getProperty("baltoro.host.id");
		if(StringUtil.isNullOrEmpty(hostId))
		{
			
			hostId = ""+(999+new Random().nextInt(8999));
			hostProps.put("baltoro.host.id", hostId);
			FileOutputStream output = new FileOutputStream(hostPropFile);
			hostProps.store(output,"");
			
		}
		
		
    	cs = new APIClient();
		props = new Properties();
		
		String propName = getMainClassName();
		String fileName = propName+"-"+Baltoro.env.toString().toUpperCase()+".env";
		
		System.out.println(fileName);
		propFile = new File(fileName);
		
    	
		
		if(!propFile.exists())
		{
			propFile.createNewFile();
		}
		
		props.load(new FileInputStream(propFile));
		
		appUuid = props.getProperty("app.uuid");
		String _appUuid = cs.getAppUuidByName(appName);
		if(StringUtil.isNullOrEmpty(_appUuid) || (appUuid ==null || !appUuid.equals(_appUuid)))
		{
			props.put("app.uuid", _appUuid);
			appUuid = _appUuid;
		}
		
		appPrivateKey = props.getProperty("app.key");
		if(StringUtil.isNullOrEmpty(appPrivateKey))
		{
			String appKey = cs.getAppData(appUuid);
			props.put("app.key", appKey);
			appPrivateKey = appKey;
		}
		
		instanceUuid = props.getProperty("app.instance.uuid");
		String _instanceUuid = cs.createInstance(_appUuid, serviceNames.toString(), instanceUuid);
		if(StringUtil.isNullOrEmpty(_instanceUuid) || (instanceUuid==null || !instanceUuid.equals(_instanceUuid)))
		{
			props.put("app.instance.uuid", _instanceUuid);
			instanceUuid = _instanceUuid;
		}
		
		if(instanceUuid == null || instanceUuid.equals("NOT ALLOWED"))
		{
			System.out.println("can't find or create an instance exiting "+appName);
			System.exit(1);
		}
		
		//hostId = instanceUuid.substring(5,9);
		//props.put("app.host.id", hostId);
		
		
		
	
		props.put("app.name", appName);
		props.put("app.env", Baltoro.env.toString());
		props.put("app.service.names", Baltoro.serviceNames.toString());
		props.put("app.server.url", Baltoro.serverURL);
		
		FileOutputStream output = new FileOutputStream(propFile);
		props.store(output,"For App "+appName.toUpperCase());
		
    	
    }
    
   
	
	static String systemIn(String msg)
	{
		try
		{
			System.out.print(msg);
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		    String input = bufferRead.readLine();
		    return input;
		} 
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
	}

}

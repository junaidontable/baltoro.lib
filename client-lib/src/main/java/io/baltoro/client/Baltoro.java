package io.baltoro.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.ws.rs.core.NewCookie;

import org.glassfish.tyrus.client.ClientManager;

import io.baltoro.ep.ClassBuilder;
import io.baltoro.ep.CloudServer;
import io.baltoro.ep.EPData;
import io.baltoro.ep.ParamInput;
import io.baltoro.to.APIError;
import io.baltoro.to.AppTO;
import io.baltoro.to.UserTO;
import io.baltoro.util.StringUtil;


public class Baltoro 
{
	
	static Logger log = Logger.getLogger(Baltoro.class.getName());
	
	static
	{
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT:%4$s > %5$s%6$s%n");
	}
	
	static Map<String, Class<?>> pathClassMap = new HashMap<String, Class<?>>(100); 
	
	
	static Map<String, NewCookie> agentCookieMap = new HashMap<String, NewCookie>(100);
	
		
	static List<ServicePackage> serviceList = new ArrayList<ServicePackage>();
	
	static StringBuffer serviceNames = new StringBuffer();
	static String hostId;
	static BOAPIClient cs;
	private static boolean logedin = false;
	static private String email;
	static private String password;
	private static UserTO user;
	static String instanceUuid;
	static int instanceThreadCount = 3;
	public static boolean debug = false;
	static Properties props = null;
	static String appUuid;
	static String appPrivateKey;
	static String appName;
	static String userUuid;
	static File propFile;
	
	private static BaltoroWSHeartbeat mgntThread;
	static WSRequestPoller requestPoller;
	static WSResponsePoller responsePoller;
	
	static HTTPRequestPoller httpRequestPoller;
	
	
	static String lcp;
	static long repMillis;
	

	
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
	
	private static Session startWSClient() throws Exception
	{
			
		
		
		/*
		int count = cs.getRemainingInsanceThreadsCount(appName, serviceName);
	
		System.out.println(" ++++++++Allowed count +++++++++++++ "+count);
		Baltoro.instanceThreadCount = count;
		
		if(count < 1)
		{
			System.out.println("Exceed allowed count , exiting");
			System.exit(1);
		}
		*/
		
		int count = 1;
		
		ExecutorService executor = Executors.newFixedThreadPool(count);
		for (int i = 0; i <count; i++)
		{
			Future<Session> future = executor.submit(new WSClient());
			Session session = future.get();

			//ClientWSSession csession = new ClientWSSession(session);
			WSSessions.get().addSession(session);
			
			log.info(" >>>>>>>>>>>>>>>>>>>>>>>>>>> started client THREAD : "+session.getId()+" ,,, i="+i);
		}
		
			
		mgntThread = new BaltoroWSHeartbeat();
		mgntThread.start();
	
		
		requestPoller = new WSRequestPoller();
		requestPoller.start();
		
		responsePoller = new WSResponsePoller();
		responsePoller.start();
	 	
		return null;
		
	}
	
	public static LocalDB getDB()
	{
		return LocalDB.instance(false, false);
	}
	
	public static LocalDB getDB(boolean clean, boolean replicate)
	{
		return LocalDB.instance(clean, replicate);
	}
	
	public static String getMainClassName() 
	{ 
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        StackTraceElement ste = stElements[stElements.length-1];
    
   
        return ste.getClassName();
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
	
	
	public static <T> T callSync(String appName, String path, Class<T> returnType, ParamInput input)
	{
		try
		{
			CloudServer cServer = new CloudServer(appName);
			EPData epData = input.getEPData();
			
			
			T t = cServer.call(path, epData, returnType);
			return t;
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	public static Future<?> callAsync(String appName, String path, Class<?> returnType, ParamInput input)
	{
		try
		{
			CloudServer cServer = new CloudServer(appName);
			EPData epData = input.getEPData();
				
			Future<?> f = cServer.callAsyn(path, epData, returnType);
			
			
			return f;
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	public static void setUserToSession(String name)
	{
		//RequestContext rc = RequestWorker.requestCtx.get();
		String userSessionId = RequestWorker.userSessionIdCtx.get();
		if(userSessionId == null)
		{
			return;
		}
		
		UserSession userSession = SessionManager.getSession(userSessionId);
		userSession.userName = name;
		userSession.sendSession();
		
	}
	
	
	public static UserSession getUserSession()
	{
		//RequestContext rc = RequestWorker.requestCtx.get();
		String userSessionId = RequestWorker.userSessionIdCtx.get();
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
		
		String userSessionId = RequestWorker.userSessionIdCtx.get();
		if(userSessionId == null)
		{
			return;
		}
		
		UserSession userSession = SessionManager.getSession(userSessionId);
		userSession.userName = null;
		userSession.invlaidateSession = true;
		
		SessionManager.removeUserSession(userSessionId);
		userSession.sendSession();
	}
	
	public static void init(String appName)
	{
		Baltoro.appName = appName;
	}
	
	public static void addService(String serviceName, String ... packageNames)
	{
		if(StringUtil.isNullOrEmpty(serviceName))
		{
			serviceName = "/";
		}
		
		ServicePackage sp = new ServicePackage(serviceName, packageNames);
		serviceList.add(sp);
		
		serviceNames.append(serviceName+",");
	}
	
	private static void start(String appName, String serviceName, String _package)
	{
	
		String _debug = System.getProperty("baltoro.debug");
		
		System.out.println("running mod : "+_debug);
		if(_debug != null && _debug.equals("true"))
		{
			Baltoro.debug = true;
		}
		
		//Baltoro.packages = _package;
		Baltoro.appName = appName;
	
		//System.out.println("packages= "+Baltoro.packages);
		
		//Baltoro.serviceName = serviceName;
 
		//System.out.println("servieName="+Baltoro.serviceName);
		
		//Session session = _start();
		//System.out.println(session);
	}
	
	
	
	public static Session start()
	{
		String _debug = System.getProperty("baltoro.debug");
		
		System.out.println("running mod : "+_debug);
		if(_debug != null && _debug.equals("true"))
		{
			Baltoro.debug = true;
		}
		
		try
		{
			
			boolean loaded = loadProperties();
			if(!loaded)
			{
				
				FileOutputStream output = new FileOutputStream(propFile);
				//AppTO selectedApp = getMyApp();
				
				appUuid = cs.getAppUuidByName(appName);
				props.put("app.uuid", appUuid);
	    		
	    		
	    		
	    		String instUuid = cs.createInstance(appUuid, serviceNames.toString());
	    		if(instUuid == null || instUuid.equals("NOT ALLOWED"))
	    		{
	    			System.out.println("can't find or create an instance exiting "+appName);
	    			System.exit(1);
	    		}
	    		
	    		Baltoro.instanceUuid = instUuid;
	    		props.put("app.instance.uuid", instUuid);
	    		
	    		
				//PrivateDataTO to = callSync("admin", "/api/app/get", PrivateDataTO.class, a -> a.add("base-uuid", selectedApp.privateDataUuid));
				
	    		String appKey = cs.getAppData(appUuid);
				props.put("app.key", appKey);
				
				
				hostId = ""+new Random().nextInt();
				props.put("app.host.id", hostId);
				
				props.store(output, "updated on "+new Date());
				
			}
			
			
			buildService();
			
			Session session = startWSClient();
			return session;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void start1()
	{
		String _debug = System.getProperty("baltoro.debug");
		
		System.out.println("running mod : "+_debug);
		if(_debug != null && _debug.equals("true"))
		{
			Baltoro.debug = true;
		}
		
		try
		{
			
			boolean loaded = loadProperties();
			if(!loaded)
			{
				
				FileOutputStream output = new FileOutputStream(propFile);
				//AppTO selectedApp = getMyApp();
				
				appUuid = cs.getAppUuidByName(appName);
				props.put("app.uuid", appUuid);
	    		
	    		
	    		
	    		String instUuid = cs.createInstance(appUuid, serviceNames.toString());
	    		if(instUuid == null || instUuid.equals("NOT ALLOWED"))
	    		{
	    			System.out.println("can't find or create an instance exiting "+appName);
	    			System.exit(1);
	    		}
	    		
	    		Baltoro.instanceUuid = instUuid;
	    		props.put("app.instance.uuid", instUuid);
	    		
	    		
				//PrivateDataTO to = callSync("admin", "/api/app/get", PrivateDataTO.class, a -> a.add("base-uuid", selectedApp.privateDataUuid));
				
	    		String appKey = cs.getAppData(appUuid);
				props.put("app.key", appKey);
				
				
				hostId = ""+new Random().nextInt();
				props.put("app.host.id", hostId);
				
				props.store(output, "updated on "+new Date());
				
			}
			
			
			buildService();
			
			
			httpRequestPoller = new HTTPRequestPoller();
			httpRequestPoller.start();
			
			
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	
	}
	
	
    public static void main(String[] args )
    {
    	//Baltoro.start("io", "/*");
    	
    	//Baltoro.start("io");
    }
    
    
    private static boolean loadProperties() throws Exception
    {
    	  
    	cs = new BOAPIClient();
		props = new Properties();
		//adminEP = endPointFactory(AdminEP.class);
		
		String propName = getMainClassName();
		String fileName = propName+".props";
		
		System.out.println(fileName);
		propFile = new File(fileName);
		
    	if(propFile.exists())
    	{
    		
    		props.load(new FileInputStream(propFile));
    		appPrivateKey = props.getProperty("app.key");
    		if(StringUtil.isNullOrEmpty(appPrivateKey))
    		{
    			return false;
    		}
    		
    		
    		appUuid = props.getProperty("app.uuid");
    		if(StringUtil.isNullOrEmpty(appUuid))
    		{
    			return false;
    		}
    		
    		instanceUuid = props.getProperty("app.instance.uuid");
    		if(StringUtil.isNullOrEmpty(instanceUuid))
    		{
    			return false;
    		}
    		
    		hostId = props.getProperty("app.host.id");
    		if(StringUtil.isNullOrEmpty(hostId))
    		{
    			return false;
    		}
    		
    		return true;
    		
    		/*
    		String option = systemIn("Start "+appName+" ? [y/n] : ");
    		if(option.equals("n"))
    		{
    			propFile.delete();
    		}
    		else
    		{
    			return true;
    		}
    		//*/
    	
    	}
    	
    	return false;
    	
		/*
    	String option = systemIn("Do you have an account ? [y/n] : ");
    	for (int i = 0; i < 3; i++)
		{
    		email = systemIn("email : ");
			password = systemIn("password : ");
			
			try
			{
				if(option.toLowerCase().equals("n"))
				{
					user = callSync("admin","/api/app/createUser", UserTO.class, a -> a.add("email", email).add("password", password));
				}
					
				ParamInput input = a -> a.add("email", email).add("password", password);
			
				user = callSync("admin", "/api/adminlogin",UserTO.class, input);
				
				userUuid = user.uuid;
				logedin = true;
			
				
				props.put("user.email", Baltoro.email);
				props.put("user.uuid", user.uuid);
				//props.put("packages", Baltoro.packages);
	    		//props.put("cluster.path", Baltoro.clusterPath);
	    	
				
				return false;
			} 
			catch (RuntimeException e)
			{
				System.out.println("=====> "+e.getMessage());
			}
			
		}
    	
    	System.out.println("Would not process sfter 3 tries. restart the program");
		System.exit(1);
		
		return false;
		
		*/
    }
    
    private static AppTO getMyApp() throws Exception
    {
    	if(!logedin)
    		return null;
    	
    	/*
    	String lastAppUuid = db.get(OName.APP_UUID);
    	if(lastAppUuid !=  null)
    	{
    		AppTO appTO = new AppTO();
    		appTO.uuid = lastAppUuid;
    		
    		String appPublicKey = db.get(OName.APP_PUBLIC_KEY);
    		appTO.publicKey = appPublicKey;
    		
    		String _privKey = db.get(OName.APP_PRIVATE_KEY);
    		String appPrivKey = CryptoUtil.decryptWithPassword(password, _privKey);
    		appTO.privateKey = appPrivKey;
    		
    		return appTO;
    	}
    	*/
    	
    	boolean newApp = true;
    	
    	AppTO selectApp = null;
    	
		//AppTO[] apps = adminEP.getMyApps();
    	AppTO[] apps = callSync("admin", "/api/app/getMyApps", AppTO[].class, a -> a);
		
		if(apps.length > 0)
		{
			newApp = false;
			System.out.println(" ========  apps ========= ");
			System.out.println("0 -- to create new app : ");
			
			System.out.println(" ======== exisiting apps ========= ");
			int i = 1;
			for (AppTO appTO : apps)
			{
				System.out.println(i++ +" -- to start app : "+appTO.name);
			}
			String option = systemIn("enter option : ");
			
			if(option.equals("0"))
			{
				newApp = true;
			}
			else
			{
				int opt = Integer.parseInt(option);
				selectApp = apps[opt-1];
				System.out.println("selected app : "+selectApp.name);
			}
			
		}
		
		if(newApp)
		{
			
			AppTO to = null;
			
			for (int i = 0; i < 5; i++)
			{
				try
				{
					String name = systemIn("enter name of your new app : ");
					//to = adminEP.createApp(name);
					
					to = callSync("admin","/api/app/createApp", AppTO.class, a -> a.add("name", name));
					break;
				} 
				catch (APIError e)
				{
					System.out.println("************************");
					System.out.println(e.getMessage());
					System.out.println("************************");
				}
			}
			
			if(to == null)
			{
				System.out.println("5 tries, restart the app again");
				System.exit(1);
			}
			
			selectApp = to;
			
		}
		
		
		System.out.println(" =-==== "+selectApp.privateDataUuid);
		
		props.put("app.uuid", selectApp.uuid);
		props.put("app.name", selectApp.name);
		
		
		/*
		db.save(OName.APP_UUID, selectedApp.uuid);
		String appPrivKey = CryptoUtil.encryptWithPassword(password, selectedApp.privateKey);
		db.save(OName.APP_PRIVATE_KEY, appPrivKey);
		db.save(OName.APP_PUBLIC_KEY, selectedApp.publicKey);
		*/
		
		return selectApp;
				
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
	
	
	public static Session connectWebSocket(boolean debug, String appName, String path, MessageHandler.Whole<ByteBuffer> handlerClass)
	{
		try
		{
			ClientManager clientManager = ClientManager.createClient();
	 	    BaltoroClientConfigWSWeb clientConfigurator = new BaltoroClientConfigWSWeb(appName,path, "token");
	 	    
	 	    ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
	                 .configurator(clientConfigurator)
	                 .build();
	 	    
	 	  
	 	  String url = null;
	 	 
	 	 Baltoro.debug = debug;
	 	 
	 	  if(Baltoro.debug)
	 	  {
	 		 url = "ws://"+appName+".baltoro.io:8080/"+path;
	 		 // url = "ws://super-server:8080/"+path;
	 	  }
	 	  else
	 	  {
	 		 url = "ws://"+appName+".baltoro.io/"+path;
	 	  }
	 	  //*/
	 	  
	 	  //url = "ws://localhost:8080/probe1";
	 	  
	 	 // url = "ws://admin.baltoro.io/"+path;
	 	  
	 	 //url = "ws://127.0.0.1:8080/"+path;
	 	 
	 	  BaltoroClientEndpointWSWeb instance = new BaltoroClientEndpointWSWeb(appName, path, handlerClass);
	 	 
	 	  Session session = clientManager.connectToServer(instance, config, new URI(url));
	 	  
	 	  return session ;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}

	
}

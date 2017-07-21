package io.baltoro.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.websocket.Session;
import javax.ws.rs.core.NewCookie;

import io.baltoro.ep.ClassBuilder;
import io.baltoro.ep.CloudServer;
import io.baltoro.ep.EPData;
import io.baltoro.ep.ParamInput;
import io.baltoro.to.APIError;
import io.baltoro.to.AppTO;
import io.baltoro.to.PrivateDataTO;
import io.baltoro.to.RequestContext;
import io.baltoro.to.UserTO;


public class Baltoro 
{
	
	static Logger log = Logger.getLogger(Baltoro.class.getName());
	
	static
	{
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT:%4$s > %5$s%6$s%n");
	}
	
	static Map<String, Class<?>> pathClassMap = new HashMap<String, Class<?>>(100); 
	
	
	static Map<String, NewCookie> agentCookieMap = new HashMap<String, NewCookie>(100);
	
	static String packages;
	private static BOAPIClient cs;
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
	static RequestPoller requestPoller;
	static ResponsePoller responsePoller;
	static String clusterPath = "/*";
	//private static AdminEP adminEP;	

	
	private Baltoro()
	{
		
	}
	
	
	private static Session startClient() throws Exception
	{
			
		Map<String, WebMethod> pathMap = new HashMap<String, WebMethod>(200);
		
		AnnotationProcessor p = new AnnotationProcessor();
		for (String _package : packages.split(","))
		{
			Map<String, WebMethod> pMap = p.processAnnotation(_package.trim());
			pathMap.putAll(pMap);
		}
		
		WebMethodMap.getInstance().setMap(pathMap);
		
		String instUuid = cs.getInstanceUuid(appUuid);
		if(instUuid == null || instUuid.equals("NOT ALLOWED"))
		{
			System.out.println("can't find or create an instance exiting "+appUuid);
			System.exit(1);
		}
		
		Baltoro.instanceUuid = instUuid;
		
		int count = cs.getRemainingInsanceThreadsCount(appUuid, instanceUuid);
	
		System.out.println(" ++++++++Allowed count +++++++++++++ "+count);
		Baltoro.instanceThreadCount = count;
		
		if(count < 1)
		{
			System.out.println("Exceed allowed count , exiting");
			System.exit(1);
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(count);
		for (int i = 0; i <count; i++)
		{
			Future<Session> future = executor.submit(new WSClient());
			Session session = future.get();

			ClientWSSession csession = new ClientWSSession(session);
			WSSessions.get().addSession(csession);
		}
		
			
		mgntThread = new BaltoroWSHeartbeat();
		mgntThread.start();
	
		
		requestPoller = new RequestPoller();
		requestPoller.start();
		
		responsePoller = new ResponsePoller();
		responsePoller.start();
	 	
		return null;
		
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
	
	
	public static <T> T call(String appName, String path, Class<T> returnType, ParamInput input)
	{
		try
		{
			CloudServer cServer = new CloudServer(appName);
			ParamInput _input = input.get(input);
			
			EPData epData = _input.getEPData();
			
			
			T t = cServer.call(path, epData, returnType);
			return t;
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	public static void setUserToSession(String name)
	{
		RequestContext rc = RequestWorker.requestCtx.get();
		if(rc == null)
		{
			return;
		}
		
		UserSession userSession = SessionManager.getSession(rc.getSessionId());
		userSession.userName = name;
		userSession.sendSession();
		
	}
	
	
	public static UserSession getUserSession()
	{
		RequestContext rc = RequestWorker.requestCtx.get();
		UserSession userSession = SessionManager.getSession(rc.getSessionId());
		return userSession;
	}
	
	public static void invalidateSession()
	{
		RequestContext rc = RequestWorker.requestCtx.get();
		UserSession userSession = SessionManager.getSession(rc.getSessionId());
		userSession.userName = null;
		userSession.invlaidateSession = true;
		
		SessionManager.removeUserSession(rc.getSessionId());
		userSession.sendSession();
	}
	
	/*
	public static void startDebug(String _package, String clusterPath)
	{
		packages = _package;
		Baltoro.clusterPath = clusterPath != null ? clusterPath : Baltoro.clusterPath;
		Baltoro.debug = true;
		Session session = _start();
		System.out.println(session);
	}
	*/
	
	public static void start()
	{
		String _package = getMainClassPackageName();
		start(_package, clusterPath);
	}
	
	public static void start(String clusterPath)
	{
		String _package = getMainClassPackageName();
		start(_package, clusterPath);
	}
	
	public static void startDebug()
	{
		System.setProperty("baltoro.debug", "true");
		String _package = getMainClassPackageName();
		start(_package, clusterPath);
	}
	
	public static void startDebug(String clusterPath)
	{
		System.setProperty("baltoro.debug", "true");
		String _package = getMainClassPackageName();
		start(_package, clusterPath);
	}
	
	public static void startDebug(String _package, String clusterPath)
	{
		System.setProperty("baltoro.debug", "true");
		start(_package, clusterPath);
	}
	
	public static void start(String _package, String clusterPath)
	{
	
		String _debug = System.getProperty("baltoro.debug");
		
		System.out.println("running mod : "+_debug);
		if(_debug != null && _debug.equals("true"))
		{
			Baltoro.debug = true;
		}
		
		packages = _package;
		String _packages = System.getProperty("baltoro.packages");
		System.out.println("-D.baltoro.packages= "+_packages);
		if(_packages != null)
		{
			Baltoro.packages = _packages;
		}
		System.out.println("packages= "+Baltoro.packages);
		
		Baltoro.clusterPath = clusterPath != null ? clusterPath : Baltoro.clusterPath;
		String _clusterPath = System.getProperty("baltoro.clusterPath");
		System.out.println("-D.baltoro.clusterPath="+_clusterPath);
		if(_clusterPath != null)
		{
			Baltoro.clusterPath = _clusterPath;
		}
		System.out.println("clusterPathh="+Baltoro.clusterPath);
		
		Session session = _start();
		System.out.println(session);
	}
	
	private static Session _start()
	{
		try
		{
			
			boolean useLocal = init();
			if(!useLocal)
			{
				
				FileOutputStream output = new FileOutputStream(propFile);
				AppTO selectedApp = getMyApp();
				
				appUuid = selectedApp.uuid;
	    		appName = selectedApp.name;
	    		
				
				//PrivateDataTO to = adminEP.getBO(selectedApp.privateDataUuid, PrivateDataTO.class);
	    		
	    		PrivateDataTO to = call("admin", "/api/app/get", PrivateDataTO.class, a -> a.add("base-uuid", selectedApp.privateDataUuid));
				props.put("app.key", to.privateKey);
				props.store(output, "updated on "+new Date());
			}
			
			
			Session session = startClient();
			return session;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	
    public static void main(String[] args )
    {
    	Baltoro.start("io", "/*");
    	
    	//Baltoro.start("io");
    }
    
    /*
    void init_db() throws Exception
    {
    	cs = new BOAPIClient(this);
    	
    	db = new LocalDB(this);
    	boolean isSetup = db.isSetup();
		if(!isSetup)
		{
			setupDB();
			return;	
		}
	

		for (int i = 0; i < 3; i++)
		{
			password = systemIn("enter password to login (enter 0 to reset) : ");
			if(password.equals("0"))
			{
				db.cleanUp();
				System.out.println("cleanup complete restart the program");
				System.exit(1);
			}
			
			email = db.login(password);
			
			if(email == null)
			{
				System.out.println("wrong password, retry");
				continue;
			}
			
			UserTO userTO = cs.login(email, password);
			logedin = true;
			break;
			
		}
				
		if(!logedin)
		{
			System.out.println("could not login");
			System.exit(1);
		}
				
			
	}
    */
    
    private static boolean init() throws Exception
    {
    	  
    	cs = new BOAPIClient();
		props = new Properties();
		//adminEP = endPointFactory(AdminEP.class);
		
		String propName = getMainClassName();
		String fileName = "."+propName+".props";
		
		System.out.println(fileName);
		propFile = new File(fileName);
		
    	if(false)//propFile.exists())
    	{
    		
    		props.load(new FileInputStream(propFile));
    		appPrivateKey = props.getProperty("app.key");
    		appUuid = props.getProperty("app.uuid");
    		appName = props.getProperty("app.name");
    		userUuid = props.getProperty("user.uuid");
    		email = props.getProperty("user.email");
    		
    		//instanceUuid = props.getProperty("app.instance.uuid");
    		//packages = props.getProperty("packages", Baltoro.packages);
    		//clusterPath = props.getProperty("cluster.path", Baltoro.clusterPath);
    		
    		
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
    		*/
    	
    	}
    	
		
    	String option = systemIn("Do you have an account ? [y/n] : ");
    	for (int i = 0; i < 3; i++)
		{
    		email = systemIn("email : ");
			password = systemIn("password : ");
			
			try
			{
				if(option.toLowerCase().equals("n"))
				{
					//user = adminEP.createUser(email, password);
					
					user = call("admin","/api/app/createUser", UserTO.class, a -> a.add("email", email).add("password", password));
				}
				
				
				
				//user = adminEP.adminLogin(email, password);
				
				/*
				user = call("admin", "/api/adminlogin",UserTO.class, new ParamInput()
				{	
					@Override
					public void input()
					{
						add("email", email);
						add("password", password);
						
					}
				});
				*/
				
				//ParamInput input = new ParamInput(){}.add("email", email).add("password", password);
				
					
				ParamInput input = a -> a.add("email", email).add("password", password);
			
				user = call("admin", "/api/adminlogin",UserTO.class, input);
				
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
    	AppTO[] apps = call("admin", "/api/app/getMyApps", AppTO[].class, a -> a);
		
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
					
					to = call("admin","/api/app/createApp", AppTO.class, a -> a.add("name", name));
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

	
}

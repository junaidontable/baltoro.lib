package io.baltoro.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.websocket.Session;
import javax.ws.rs.core.NewCookie;

import io.baltoro.client.util.UUIDGenerator;
import io.baltoro.ep.ClassBuilder;
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
	
	
	Map<String, NewCookie> agentCookieMap = new HashMap<String, NewCookie>(100);
	
	private String packages;
	private BOAPIClient cs;
	boolean logedin = false;
	static private String email;
	static private String password;
	static String sessionId;
	UserTO user;
	static String instanceUuid;
	boolean debug = false;
	Properties props = null;
	static String appUuid;
	static String appPrivateKey;
	static String appName;
	static String userUuid;
	File propFile = new File(".baltoro.props");
	BaltoroWSHeartbeat mgntThread;
	RequestPoller requestPoller;
	ResponsePoller responsePoller;
	static String clusterPath = "/*";
	 
	
	private Baltoro()
	{
		
	}
	
	
	private Session startClient() throws Exception
	{
		this.sessionId = UUID.randomUUID().toString();
		
		Map<String, WebMethod> pathMap = new HashMap<String, WebMethod>(200);
		
		AnnotationProcessor p = new AnnotationProcessor();
		for (String _package : this.packages.split(","))
		{
			Map<String, WebMethod> pMap = p.processAnnotation(_package);
			pathMap.putAll(pMap);
		}
		
		WebMethodMap.getInstance().setMap(pathMap);
		
		
	
		/*
		Future<Session> future = executor.submit(() -> 
		{
		    try 
		    {
		    	ClientManager clientManager = ClientManager.createClient();
		 	    BaltoroClientConfigurator clientConfigurator = new BaltoroClientConfigurator(this.agentCookieMap, this.appUuid, this.instanceUuid, eToken);
		 	    
		 	    ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
		                 .configurator(clientConfigurator)
		                 .build();
		 	    
		 	  
		 	  String url = null;
		 	  if(this.debug)
		 	  {
		 		 url = "ws://"+this.appUuid+".baltoro.io:8080/ws";
		 	  }
		 	  else
		 	  {
		 		 url = "ws://"+this.appUuid+".baltoro.io/ws";
		 	  }
		 	  
		 	  BaltoroClientEndpoint instance = new BaltoroClientEndpoint(this.appUuid, clientManager, config, url);
		 	 
		 	  Session session = clientManager.connectToServer(instance, config, new URI(url));
		 	  
		 	  return session;
		 	  
		    }
		    catch (Exception e) 
		    {
		        throw new IllegalStateException("task interrupted", e);
		    }
		});
		*/
		
		ExecutorService executor = Executors.newFixedThreadPool(8);
		Session session = null;
		for (int i = 0; i <1; i++)
		{
			Future<Session> future = executor.submit(new WSClient(this));
			session = future.get();

			ClientWSSession csession = new ClientWSSession(session);
			WSSessions.get().addSession(csession);
		}
		
		mgntThread = new BaltoroWSHeartbeat(this, session);
		mgntThread.start();
		
		requestPoller = new RequestPoller(this);
		requestPoller.start();
		
		responsePoller = new ResponsePoller(this);
		responsePoller.start();
	 	
		return null;
		
	}
	
	
	
	public static <T> T EndPointFactory(Class<T> _class)
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
			return (T)obj;
			
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
		userSession.setUserName(name);
		
	}
	
	
	public static UserSession getUserSession()
	{
		RequestContext rc = RequestWorker.requestCtx.get();
		UserSession userSession = SessionManager.getSession(rc.getSessionId());
		return userSession;
	}
	
	
	public static void startDebug(String _package, String clusterPath)
	{
		Session session = _start(_package, clusterPath, true);
	}
	
	public static void start(String _package, String clusterPath)
	{
		Session session = _start(_package, clusterPath, false);
	}
	
	private static Session _start(String _package, String clusterPath, boolean debug)
	{
		try
		{
			Baltoro baltoro = new Baltoro();
			baltoro.debug = debug;
			Baltoro.clusterPath = clusterPath != null ? clusterPath : Baltoro.clusterPath;
			
			boolean useLocal = baltoro.init();
			if(!useLocal)
			{
				FileOutputStream output = new FileOutputStream(baltoro.propFile);
				AppTO selectedApp = baltoro.getMyApp();
				PrivateDataTO to = baltoro.cs.getBO(selectedApp.privateDataUuid, PrivateDataTO.class);
				baltoro.props.put("app.key", to.privateKey);
				baltoro.props.store(output, "updated on "+new Date());
			}
			
			baltoro.packages = _package;
			Session session = baltoro.startClient();
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
    	Baltoro.startDebug("io", "/*");
    	
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
    
    private boolean init() throws Exception
    {
    	cs = new BOAPIClient(this);
		props = new Properties();
		
    	if(propFile.exists())
    	{
    		
    		props.load(new FileInputStream(propFile));
    		appPrivateKey = props.getProperty("app.key");
    		appUuid = props.getProperty("app.uuid");
    		appName = props.getProperty("app.name");
    		userUuid = props.getProperty("user.uuid");
    		email = props.getProperty("user.email");
    		instanceUuid = props.getProperty("app.instance.uuid");
    		
    		
    		
    		String option = systemIn("Start "+appName+" ? [y/n] : ");
    		if(option.equals("n"))
    		{
    			propFile.delete();
    		}
    		else
    		{
    			return true;
    		}
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
					user = cs.createUser(email, password);
				}
				user = cs.login(email, password);
				logedin = true;
				
				props.put("user.email", this.email);
				props.put("user.uuid", user.uuid);
				this.instanceUuid = UUIDGenerator.uuid("INST");
				props.put("app.instance.uuid",this.instanceUuid);
				
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
    
    AppTO getMyApp() throws Exception
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
    	
		List<AppTO> apps = cs.getMyApps();
		
		if(apps.size() > 0)
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
				selectApp = apps.get(opt-1);
				System.out.println("selected app : "+selectApp.name);
			}
			
		}
		
		if(newApp)
		{
			String name = systemIn("enter name of your new app : ");
			AppTO to = cs.createApp(name);
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
	
	String systemIn(String msg)
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

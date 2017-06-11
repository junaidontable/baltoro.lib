package io.baltoro.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Session;
import javax.ws.rs.core.NewCookie;

import org.glassfish.tyrus.client.ClientManager;

import io.baltoro.ep.ClassBuilder;
import io.baltoro.to.AppTO;
import io.baltoro.to.Principal;
import io.baltoro.to.PrivateDataTO;
import io.baltoro.to.UserTO;
import io.baltoro.util.UUIDGenerator;

public class Baltoro 
{
	
	static Logger log = Logger.getLogger(Baltoro.class.getName());
	
	static
	{
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT:%4$s > %5$s%6$s%n");
	}
	
	private static Map<String, Class<?>> classMap = new HashMap<String, Class<?>>(); 
	
	Map<String, NewCookie> agentCookieMap = new HashMap<String, NewCookie>(100);
	
	private String packages;
	//private LocalDB db;
	private BOAPIClient cs;
	boolean logedin = false;
	private String email;
	private String password;
	String sessionId;
	UserTO user;
	AppTO currentApp;
	PrivateDataTO currentAppPrivateData;
	String instanceUuid;
	boolean debug = false;
	
	 
	
	private Baltoro()
	{
		
	}
	
	
	private Session startClient() throws Exception
	{
		
		Map<String, WebMethod> pathMap = new HashMap<String, WebMethod>(200);
		
		AnnotationProcessor p = new AnnotationProcessor();
		for (String _package : this.packages.split(","))
		{
			Map<String, WebMethod> pMap = p.processAnnotation(_package);
			pathMap.putAll(pMap);
		}
		
		WebMethodMap.getInstance().setMap(pathMap);
		
		//BaltoroWSClient client = new BaltoroWSClient(this.appUuid, this.sessionId);
		//client.start();
	
		
		ExecutorService executor = Executors.newWorkStealingPool();
		
		Future<Session> future = executor.submit(() -> 
		{
		    try 
		    {
		    	ClientManager clientManager = ClientManager.createClient();
		 	    BaltoroClientConfigurator clientConfigurator = new BaltoroClientConfigurator(this.agentCookieMap, this.currentApp.uuid, this.instanceUuid);
		 	    
		 	    ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
		                 .configurator(clientConfigurator)
		                 .build();
		 	    
		 	  BaltoroClientEndpoint instance = new BaltoroClientEndpoint(this.currentApp.uuid);
		 	  String url = null;
		 	  if(this.debug)
		 	  {
		 		 url = "ws://"+this.currentApp.uuid+".baltoro.io:8080/baltoro/ws";
		 	  }
		 	  else
		 	  {
		 		 url = "ws://"+this.currentApp.uuid+".baltoro.io/baltoro/ws";
		 	  }
		 	  Session session = clientManager.connectToServer(instance, config, new URI(url));
		 	  
		 	  return session;
		 	  
		    }
		    catch (Exception e) 
		    {
		        throw new IllegalStateException("task interrupted", e);
		    }
		});
		
		
		Session session = future.get();
		
		BaltoroWSPing thread = new BaltoroWSPing(session);
	 	thread.start();
	 	  
		return session;
		
	}
	
	
	public static <T> T EndPointFactory(Class<T> _class)
	{
		try
		{
			Class<?> implClass = classMap.get(_class.getName());
			if(implClass == null)
			{
				ClassBuilder builder = new ClassBuilder(_class);
				implClass = builder.buildClass();
				classMap.put(_class.getName(), implClass);
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
	
	
	public static void asignPrincipalToSession(String sessionId, Principal principal)
	{
		UserSession userSession = SessionManager.getSession(sessionId);
		userSession.principal = principal;
	}
	
	
	public static UserSession getUserSession(String sessionId)
	{
		UserSession userSession = SessionManager.getSession(sessionId);
		return userSession;
	}
	
	
	public static Session start(String _package, boolean debug)
	{
		
		try
		{
			Baltoro baltoro = new Baltoro();
			baltoro.debug = debug;
			baltoro.init();
			baltoro.currentApp = baltoro.getMyApp();
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
	
	public static Session start(String _package)
	{
		try
		{
			Baltoro baltoro = new Baltoro();
			baltoro.init();
			baltoro.currentApp = baltoro.getMyApp();
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
    	Baltoro.start("io", true);
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
    
    private void init() throws Exception
    {
    	cs = new BOAPIClient(this);
		this.instanceUuid = UUIDGenerator.randomString(10);
		
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
				return;
			} 
			catch (RuntimeException e)
			{
				System.out.println("=====> "+e.getMessage());
			}
			
		}
    	
    	System.out.println("Would not process sfter 3 tries. restart the program");
		System.exit(1);
		
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
				currentApp = apps.get(opt-1);
				System.out.println("selected app : "+currentApp.name);
			}
			
		}
		
		if(newApp)
		{
			String name = systemIn("enter name of your new app : ");
			AppTO to = cs.createApp(name);
			currentApp = to;
			
		}
		
		
		System.out.println(" =-==== "+currentApp.privateDataUuid);
		
		this.currentAppPrivateData = cs.getBO(currentApp.privateDataUuid, PrivateDataTO.class);
		
		
		/*
		db.save(OName.APP_UUID, selectedApp.uuid);
		String appPrivKey = CryptoUtil.encryptWithPassword(password, selectedApp.privateKey);
		db.save(OName.APP_PRIVATE_KEY, appPrivKey);
		db.save(OName.APP_PUBLIC_KEY, selectedApp.publicKey);
		*/
		
		return currentApp;
				
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

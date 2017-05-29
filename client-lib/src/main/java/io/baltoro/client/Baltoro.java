package io.baltoro.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;

import io.baltoro.ep.ClassBuilder;
import io.baltoro.to.AppTO;
import io.baltoro.to.Keys;
import io.baltoro.to.UserTO;
import io.baltoro.util.CryptoUtil;

public class Baltoro 
{
	
	static Logger log = Logger.getLogger(Baltoro.class.getName());
	
	static
	{
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT:%4$s > %5$s%6$s%n");
	}
	
	private static Map<String, Class<?>> classMap = new HashMap<String, Class<?>>(); 
	private String appUuid;
	private String packages;
	private LocalDB db;
	private BOAPIClient cs;
	 String privateKey;
	 String publicKey;
	boolean logedin = false;
	private String email;
	private String password;
	String  appPrivateKey;
	String 	appPublicKey;
	String sessionId;
	
	 
	
	private Baltoro()
	{
		try
		{
			init();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	private void startClient() throws Exception
	{
		
		Map<String, WebMethod> pathMap = new HashMap<String, WebMethod>(200);
		
		AnnotationProcessor p = new AnnotationProcessor();
		for (String _package : this.packages.split(","))
		{
			Map<String, WebMethod> pMap = p.processAnnotation(_package);
			pathMap.putAll(pMap);
		}
		
		WebMethodMap.getInstance().setMap(pathMap);
		
		BaltoroWSClient client = new BaltoroWSClient(this.appUuid, this.sessionId);
		client.start();
		
		
	}
	
	
	public static <T> T EndPointFactory(Class<T> _class)
	{
		try
		{
			Class implClass = classMap.get(_class.getName());
			if(implClass == null)
			{
				ClassBuilder builder = new ClassBuilder(_class);
				implClass = builder.buildClass();
				classMap.put(_class.getName(), implClass);
			}
			
			//Class<?> implClass = Class.forName(_class.getName()+"Impl");
			Object obj = implClass.newInstance();
			return (T)obj;
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	public static void start()
	{
		try
		{
			Baltoro baltoro = new Baltoro();
			AppTO appTo = baltoro.getMyApp();
			baltoro.appUuid = appTo.uuid;
			baltoro.packages = "com";
			baltoro.appPrivateKey = appTo.privateKey;
			baltoro.appPublicKey = appTo.publicKey;
			
			baltoro.startClient();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
    public static void main(String[] args )
    {
    	Baltoro.start();
    }
    
    void init() throws Exception
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
    
    
    private void setupDB() throws Exception
    {
    	UserTO user = null;
		Keys keys = CryptoUtil.generateKeys();
		privateKey = keys.getPrivateKey();
		publicKey = keys.getPublicKey();
		
		String option = systemIn("enter 1 to signup : \nenter 2 to login : ");
		
		email = systemIn("email : ");
		password = systemIn("password : ");

		try
		{
			if(option.equals("1"))
			{
				user = cs.createUser(email, password);
			}
			user = cs.login(email, password);
			logedin = true;
			db.setup(user,password);
		} 
		catch (ProcessingException e)
		{
			System.out.println(e.getMessage());
		}
		

    }
    
    AppTO getMyApp() throws Exception
    {
    	if(!logedin)
    		return null;
    	
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
    	
    	boolean newApp = false;
    	
		List<AppTO> apps = cs.getMyApps();
		AppTO selectedApp = null;
		
		if(apps.size() > 0)
		{
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
				selectedApp = apps.get(opt-1);
				System.out.println("selected app : "+selectedApp.name);
			}
			
		}
		
		if(newApp)
		{
			String name = systemIn("create new app : ");
			AppTO to = cs.createApp(name);
			selectedApp = to;
		}
			
		db.save(OName.APP_UUID, selectedApp.uuid);
		String appPrivKey = CryptoUtil.encryptWithPassword(password, selectedApp.privateKey);
		db.save(OName.APP_PRIVATE_KEY, appPrivKey);
		db.save(OName.APP_PUBLIC_KEY, selectedApp.publicKey);
		
		return selectedApp;
				
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

package io.baltoro.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;

import org.reflections.Reflections;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.client.util.StringUtil;
import io.baltoro.features.AbstractFilter;
import io.baltoro.features.Filter;
import io.baltoro.features.NoAuth;
import io.baltoro.features.NoDiscover;
import io.baltoro.features.Param;
import io.baltoro.features.Path;
import io.baltoro.features.Timeout;
import io.baltoro.features.WS;

public class AnnotationProcessor
{

	static Logger log = Logger.getLogger(AnnotationProcessor.class.getName());
	
	Map<String, WebMethod> pathMap = new HashMap<String, WebMethod>(100);
	
	static ObjectMapper mapper = new ObjectMapper();
	
	
	public Map<String, WebMethod> processAnnotation(String serviceName, String packageName) throws Exception
	{
		if(!packageName.equals("io.baltoro.client.APITest"))
		{
			System.out.println("[[[[\nserviceName = "+serviceName+", packageName = "+packageName);
		}
		
		Set<Class<?>> classes = getClasses(packageName);
		
		
		
		for (Class<?> _class : classes)
		{
			if(_class != APITest.class)
			{
				System.out.println("class => "+_class);
			}
			
			Annotation[] annoArray =  _class.getAnnotations();
	
			
			for (Annotation anno : annoArray)
			{
			
				if(anno instanceof Filter)
				{
					Filter filterAnno = (Filter) anno;
					Class<AbstractFilter> filter = (Class<AbstractFilter>) _class;
					WebMethodMap.getInstance().addFilter(filterAnno.value(), filter);
				}
				
				String cPath = null;
				if(anno instanceof Path)
				{
					cPath = ((Path) anno).value();
				}
				
				if(anno instanceof WS)
				{
					cPath = ((WS) anno).value();
				}
				
				if(cPath == null)
				{
					continue;
				}
				
					
				for (Method method : _class.getDeclaredMethods())
				{
					//String[] serviceNames = Baltoro.serviceName.split(",");
					//for (int i = 0; i < serviceNames.length; i++)
					//{
						WebMethod wm = processPathAnno(serviceName,method, _class, cPath);
						if(wm == null)
						{
							continue;
						}
						
						Timeout timeoutAnno = method.getAnnotation(Timeout.class);
						if(timeoutAnno != null)
						{
							wm.timeoutSec = timeoutAnno.value();
						}
					//}
					
				}
					
				
				
				if(anno instanceof RolesAllowed)
				{
					RolesAllowed roleAnno = (RolesAllowed) anno;
					String[] roles = roleAnno.value();
					for (int i = 0; i < roles.length; i++)
					{
						System.out.println("role allowed"+roles[i]);
					}
					
				}
				
				
			}
			
		}
		
		if(packageName.equals("io.baltoro.client.APITest"))
		{
			return pathMap;
		}
		
		String sName = serviceName.startsWith("/") ? serviceName : "/"+serviceName;
		for (String key : pathMap.keySet())
		{
			if(key.startsWith(sName))
			{
				System.out.println("PATH -> "+key+" --> "+pathMap.get(key));
			}
		} 
			
		System.out.println("]]]]");
		
		
		return pathMap;
	}
	
	
	private WebMethod processPathAnno(String serviceName, Method method, Class<?> _class, String cPath)
	{
		WebMethod wm = null;
		
		boolean cNoAuth = _class.isAnnotationPresent(NoAuth.class);
		boolean cNoDiscover = _class.isAnnotationPresent(NoDiscover.class);
		boolean isWS = false;
		
		String fPath = null;
		
		if (method.isAnnotationPresent(Path.class))
		{
			
			Path pathAnno = (Path) method.getAnnotation(Path.class);
			
			String mPath = pathAnno.value();
			
			if(!mPath.startsWith("/"))
			{
				mPath = "/"+mPath;
			}
			
			if(cPath.equals("/"))
			{
				fPath = mPath;
			}
			else
			{
				fPath = cPath+mPath;
			}
				
			if(StringUtil.isNullOrEmpty(serviceName) || serviceName.equals("/"))
			{
				fPath = fPath.toLowerCase();
			}
			else
			{
				fPath = "/"+serviceName+fPath.toLowerCase();
			}
		}
		
		/*
		if (method.isAnnotationPresent(OnOpen.class))
		{
			fPath = cPath+"/onopen";
			isWS = true;
		}
		
		if (method.isAnnotationPresent(OnClose.class))
		{
			fPath = cPath+"/onclose";
			isWS = true;
		}
		
		if (method.isAnnotationPresent(OnMessage.class))
		{
			fPath = cPath+"/onmessage";
			isWS = true;
		}
		*/
		
		if(fPath == null)
		{
			return null;
		}
			
		
		if(fPath.endsWith("/"))
		{
			System.out.println(" =====> "+fPath);
			fPath = fPath.substring(0, fPath.length()-1);
		}
			
		
		
		wm = new WebMethod(fPath, _class, method);
		
		wm.setWebSocket(isWS);
		
		if(cNoAuth)
		{
			wm.authRequired = false;
		}
		else
		{
			wm.authRequired = method.isAnnotationPresent(NoAuth.class) ? false : true;
		}
		
		if(cNoDiscover)
		{
			wm.discoverable = false;
		}
		else
		{
			wm.discoverable = method.isAnnotationPresent(NoDiscover.class) ? false : true;
		}
		
		
		
		//wm.authRequired = pathAnno.authRequired();
		//wm.discoverable = pathAnno.discaoverable();
		
		pathMap.put(fPath, wm);
		
		
		StringBuilder mPropsJson = new StringBuilder();
		mPropsJson.append("{");
		
		Class<?> returnType = method.getReturnType();
		
		
		
		String rType = returnType == null ? "void" : returnType.getSimpleName();
		mPropsJson.append("\"output\":\""+rType+"\",");
		
		if(returnType != null && !returnType.isPrimitive())
		{
			
			Field[] fields = returnType.getFields();
			for (int i = 0; i < fields.length; i++)
			{
				
			}
		}
		
		Parameter[] methodParms = method.getParameters();
		
		mPropsJson.append("\"input\":{");
		boolean inputFound = false;
		for (int i = 0; i < methodParms.length; i++)
		{
			
			Parameter param = methodParms[i];
			Class<?> paramClass = param.getType();

			String annoName = null;
			Annotation[] annos = param.getAnnotations();
			for (int j = 0; j < annos.length; j++)
			{
				Annotation paramAnno = annos[j];
				if (paramAnno.annotationType() == Param.class)
				{
					Param annoPraram = (Param) paramAnno;
					annoName = annoPraram.value();
					inputFound = true;
					mPropsJson.append("\"param\":{");
					mPropsJson.append("\"parma-name\":\""+annoName+"\",");
					mPropsJson.append("\"data-type\":\""+paramClass.getSimpleName()+"\"");
					mPropsJson.append("},");
				}

			}
		}
		
		if(inputFound)
		{
			mPropsJson.delete(mPropsJson.length()-1, mPropsJson.length());
		}
		mPropsJson.append("}");
		mPropsJson.append('}');
		
		
		
		wm.propJson = mPropsJson.toString();
		
		
		
		return wm;
	}
	
	static Set<Class<?>> getClasses(String packageName) throws Exception
	{
		Reflections reflections = new Reflections(packageName);
		Set<Class<?>> masterClassSet = new HashSet<Class<?>>();
		
		Set<Class<?>> set = reflections.getTypesAnnotatedWith(Path.class);
		masterClassSet.addAll(set);
		
		set = reflections.getTypesAnnotatedWith(Filter.class);
		masterClassSet.addAll(set);
		
		set = reflections.getTypesAnnotatedWith(WS.class);
		masterClassSet.addAll(set);
		
		return masterClassSet;

	}
}

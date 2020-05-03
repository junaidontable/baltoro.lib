package io.baltoro;

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

import io.baltoro.features.AbstractFilter;
import io.baltoro.features.Filter;
import io.baltoro.features.NoAuth;
import io.baltoro.features.NoDiscover;
import io.baltoro.features.Param;
import io.baltoro.features.Path;
import io.baltoro.features.Timeout;
import io.baltoro.util.Log;

public class APIProcessor
{

	static Logger log = Logger.getLogger(APIProcessor.class.getName());
	
	Map<String, APIMethod> pathMap = new HashMap<>(100);
	
	static ObjectMapper mapper = new ObjectMapper();
	
	
	public Map<String, APIMethod> processAnnotation() throws Exception
	{
		
		
		Set<Class<?>> classes = getClasses();//packageName);
		
		
		
		for (Class<?> _class : classes)
		{
		
			Annotation[] annoArray =  _class.getAnnotations();
	
			
			for (Annotation anno : annoArray)
			{
			
				if(anno instanceof Filter)
				{
					Filter filterAnno = (Filter) anno;
					Class<AbstractFilter> filter = (Class<AbstractFilter>) _class;
					APIMap.getInstance().addFilter(filterAnno.value(), filter);
				}
				
				String cPath = null;
				if(anno instanceof Path)
				{
					cPath = ((Path) anno).value();
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
						APIMethod wm = processPathAnno(_class,method, cPath);
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
		
		/*
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
		*/
		
		return pathMap;
	}
	
	
	private APIMethod processPathAnno(Class<?> c, Method method, String cPath)
	{
		APIMethod wm = null;
		
		boolean cNoAuth = c.isAnnotationPresent(NoAuth.class);
		boolean cNoDiscover = c.isAnnotationPresent(NoDiscover.class);
		
		
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
				
			fPath = fPath.toLowerCase();
		}
		
		
		if(fPath == null)
		{
			return null;
		}
			
		
		if(fPath.endsWith("/"))
		{
			//System.out.println(" =====> "+fPath);
			fPath = fPath.substring(0, fPath.length()-1);
		}
			
		Log.log.info("api ====> "+fPath+" ===> "+method);
		
		wm = new APIMethod(fPath, c, method);
		
	
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
	
	static Set<Class<?>> getClasses() throws Exception
	{
		Reflections reflections = new Reflections();
		Set<Class<?>> masterClassSet = new HashSet<Class<?>>();
		
		Set<Class<?>> set = reflections.getTypesAnnotatedWith(Path.class);
		masterClassSet.addAll(set);
		
		set = reflections.getTypesAnnotatedWith(Filter.class);
		masterClassSet.addAll(set);
		
		return masterClassSet;

	}
}

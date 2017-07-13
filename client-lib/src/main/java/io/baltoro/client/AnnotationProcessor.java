package io.baltoro.client;

import java.lang.annotation.Annotation;
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
import io.baltoro.features.Param;
import io.baltoro.features.Path;

public class AnnotationProcessor
{

	static Logger log = Logger.getLogger(AnnotationProcessor.class.getName());
	
	Map<String, WebMethod> pathMap = new HashMap<String, WebMethod>(100);
	
	static ObjectMapper mapper = new ObjectMapper();
	
	
	public Map<String, WebMethod> processAnnotation(String packageName) throws Exception
	{
		log.info("packageName = "+packageName);
		
		Set<Class<?>> classes = getClasses(packageName);
		
		
		
		for (Class<?> _class : classes)
		{
			log.info("class = "+_class);
			
			Annotation[] annoArray =  _class.getAnnotations();
	
			
			for (Annotation anno : annoArray)
			{
			
				if(anno instanceof Filter)
				{
					Filter filterAnno = (Filter) anno;
					Class<AbstractFilter> filter = (Class<AbstractFilter>) _class;
					WebMethodMap.getInstance().addFilter(filterAnno.value(), filter);
				}
				
				if(anno instanceof Path)
				{
					String cPath = ((Path) anno).value();
					
					for (Method method : _class.getDeclaredMethods())
					{

						if (method.isAnnotationPresent(Path.class))
						{
							Path pathAnno = (Path) method.getAnnotation(Path.class);
							String fPath = null;
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
							
							WebMethod wm = new WebMethod(fPath, _class, method);
							wm.authRequired = pathAnno.authRequired();
							wm.discoverable = pathAnno.discaoverable();
							
							pathMap.put(fPath, wm);
							
							
							StringBuilder mPropsJson = new StringBuilder();
							mPropsJson.append("{");
							
							Class<?> returnType = method.getReturnType();
							
							String rType = returnType == null ? "void" : returnType.getSimpleName();
							mPropsJson.append("\"output\":\""+rType+"\",");
							
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
						
						}
						
						
					}
					
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
		
		
		for (String key : pathMap.keySet())
		{
			System.out.println("PATH -> "+key+" --> "+pathMap.get(key));
		} 
		
		return pathMap;
	}
	
	static Set<Class<?>> getClasses(String packageName) throws Exception
	{
		Reflections reflections = new Reflections(packageName);
		Set<Class<?>> masterClassSet = new HashSet<Class<?>>();
		
		Set<Class<?>> set = reflections.getTypesAnnotatedWith(Path.class);
		masterClassSet.addAll(set);
		
		set = reflections.getTypesAnnotatedWith(Filter.class);
		masterClassSet.addAll(set);
		
		
		
		return masterClassSet;

	}
}

package io.baltoro.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;

import org.reflections.Reflections;

public class AnnotationProcessor
{

	static Logger log = Logger.getLogger(AnnotationProcessor.class.getName());
	
	public Map<String, WebMethod> processAnnotation(String packageName) throws Exception
	{
		log.info("packageName = "+packageName);
		
		Set<Class<?>> classes = getClasses(packageName);
		
		Map<String, WebMethod> pathMap = new HashMap<String, WebMethod>();
		
		for (Class<?> _class : classes)
		{
			log.info("class = "+_class);
			
			Annotation[] annoArray =  _class.getAnnotations();
	
			FileServer fileServerAnno = _class.getAnnotation(FileServer.class);
			for (Annotation anno : annoArray)
			{
			
				if(anno instanceof Path)
				{
					String cPath = ((Path) anno).value();
					if(fileServerAnno != null)
					{
						WebMethod wm = new WebMethod(null, null);
						wm.webPath = cPath;
						wm.localFilePath = fileServerAnno.value();
						pathMap.put(cPath+"*", wm);
						
						System.out.println(" ^^^^^^^^^^^^^^^^^^^^^^^^^^^  ");
						System.out.println(" ^^^^^^^^^^^^^^^^^^^^^^^^^^^  ");
						System.out.println(" ^^^^^^^^^"+fileServerAnno.value()+"^^^^^^^^  ");
						System.out.println(" ^^^^^^^^^^^^^^^^^^^^^^^^^^^  ");
					}
					
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
								
							WebMethod wm = new WebMethod(_class, method);
							pathMap.put(fPath, wm);
						
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
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Path.class);
		return annotated;

	}
}

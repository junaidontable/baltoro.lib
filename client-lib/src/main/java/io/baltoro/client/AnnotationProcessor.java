package io.baltoro.client;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;

import org.reflections.Reflections;

public class AnnotationProcessor
{

	static Logger log = Logger.getLogger(AnnotationProcessor.class.getName());
	
	public void processAnnotation(String packageName) throws Exception
	{
		log.info("packageName = "+packageName);
		
		Set<Class<?>> classes = getClasses(packageName);
		for (Class<?> _class : classes)
		{
			log.info("class = "+_class);
			
			Annotation[] annoArray =  _class.getAnnotations();
	
			for (Annotation anno : annoArray)
			{
				if(anno instanceof AppId)
				{
					AppId appId = (AppId) anno;
					System.out.println("appId = "+appId.value());
				}
				
				if(anno instanceof Path)
				{
					Path path = (Path) anno;
					System.out.println("path = "+path.value());
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
			
			/*
			if(_class.isAnnotationPresent(Path.class))
			{
				
				Path path = (Path) _class.getAnnotation(Path.class);
				System.out.println(_class+"->"+path.value());
				
				for (Method method : _class.getDeclaredMethods())
				{

					if (method.isAnnotationPresent(Path.class))
					{
						Path mpath = (Path) method.getAnnotation(Path.class);
						System.out.println(method.getName()+"->"+mpath.value());
					
					}
				}
			
			}
			*/
		}
	}
	
	static Set<Class<?>> getClasses(String packageName) throws Exception
	{
		Reflections reflections = new Reflections(packageName);
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(AppId.class);
		return annotated;

	}
}

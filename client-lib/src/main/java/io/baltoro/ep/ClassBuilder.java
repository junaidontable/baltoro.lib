package io.baltoro.ep;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import io.baltoro.features.EPReturnType;
import io.baltoro.features.Endpoint;
import io.baltoro.features.Param;

public class ClassBuilder
{

	private Class<?> interfaze;
	
	
	public ClassBuilder(Class<?> interfaze)
	{
		this.interfaze = interfaze;
	}
	
	
	public Class<?> buildClass() throws Exception
	{
		
		StringBuffer code = new StringBuffer(); 
		Package _package = interfaze.getPackage();
		
		code.append("package "+_package.getName()+";\r\n\r\n");
		
		code.append("import io.baltoro.ep.*;\r\n\r\n");
		
		String implClassName = interfaze.getSimpleName()+"Impl";
		code.append("public class "+implClassName+" implements "+interfaze.getName()+"\r\n");
		code.append("{\r\n\r\n");
		
		
		Method[] methods = interfaze.getDeclaredMethods();
		for (Method method : methods)
		{
			boolean isEPMethod = method.isAnnotationPresent(Endpoint.class);
			if(isEPMethod)
			{
				
				String methodName = method.getName();
				Class<?> _returnType = method.getReturnType();
				String returnType = null;
				if(_returnType.isArray())
				{
					Class _c = _returnType.getComponentType();
					returnType = _c.getName()+"[]";
					
				}
				else
				{
					 returnType =_returnType.getName();
				}
				
				Endpoint ep = method.getAnnotation(Endpoint.class);
				
				
				
				EPMethod epmethod = new EPMethod(returnType, methodName, ep.appName(), ep.path());
				
				Class<?>[] parameterTypes = method.getParameterTypes();
				Parameter[] params = method.getParameters();
				int i=0;
				for (Parameter param : params)
				{
					Annotation[] paramAnnos = param.getAnnotations();
					String name = null;
					boolean isEPReturnType = false;
					for (Annotation paramAnno : paramAnnos)
					{
						if(paramAnno instanceof EPReturnType)
						{
							isEPReturnType = true;
						}
						
						if(paramAnno instanceof Param)
						{
							name = ((Param)paramAnno).value();	
						}
						
						if(name != null)
						{
							break;
						}
					}
					
					String paramTytpe = parameterTypes[i++].getName();
					epmethod.addArg(paramTytpe, name, isEPReturnType);
				}
				
				String methodSrc = epmethod.getCode();
				code.append(methodSrc+"\r\n\r\n");
				
			}
		}
		
		code.append("}\n");
		
		String source = code.toString();
		EPCompiler compiler = new EPCompiler();
		Class<?> implClass = compiler.compileClass(_package.getName(), implClassName, source);
		
		
		return implClass;
	}
}

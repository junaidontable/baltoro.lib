package io.baltoro.ep;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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
			boolean isEPMethod = method.isAnnotationPresent(EndPoint.class);
			if(isEPMethod)
			{
				String methodName = method.getName();
				String returnType = method.getReturnType().getName();
				EndPoint ep = method.getAnnotation(EndPoint.class);
				
				EPMethod epmethod = new EPMethod(returnType, methodName, ep.appName(), ep.path());
				
				Class<?>[] parameterTypes = method.getParameterTypes();
				Parameter[] params = method.getParameters();
				int i=0;
				for (Parameter param : params)
				{
					Annotation[] paramAnnos = param.getAnnotations();
					String name = null;
					for (Annotation paramAnno : paramAnnos)
					{
						
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
					epmethod.addArg(paramTytpe, name);
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

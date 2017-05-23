package io.baltoro.anno;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({ "io.baltoro.anno.EndPoint" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class EndPointProcessor extends AbstractProcessor
{

	private Filer filer;
	private Messager messager;

	@Override
	public synchronized void init(ProcessingEnvironment env)
	{
		System.out.println("********************");
		this.filer = env.getFiler();
		this.messager = env.getMessager();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env)
	{
		
		if (annotations == null || annotations.size() == 0)
		{
			return false;
		}


		TypeElement endPointElement = null;

		for (TypeElement ann : annotations)
		{
			endPointElement = ann;
			List<? extends Element> es = ann.getEnclosedElements();
			for (Element e : es)
			{
				System.out.println(" parameter name : " + e);
			}
		}

		if (endPointElement == null)
		{
			System.out.println("endPointElement is null");
			return false;
		} 
		else
		{
			System.out.println("endPointElement = " + endPointElement);
		}

		/*
		 * Element enclosingElement = endPointElement.getEnclosingElement();
		 * 
		 * System.out.println(" ====> methods = " + enclosingElement);
		 * 
		 * ElementKind kind = ann.getKind(); System.out.println(
		 * " ====> ann.getKind() = " + kind);
		 */
		
		Map<String, List<EndPointMethod>> classMap = new HashMap<String, List<EndPointMethod>>();


		Set<? extends Element> methodElements = env.getElementsAnnotatedWith(endPointElement);

		for (Element _methodElement : methodElements)
		{
			ExecutableElement methodElement = (ExecutableElement) _methodElement;
			Element classElement = methodElement.getEnclosingElement();
			
			List<EndPointMethod> methodList = classMap.get(classElement.toString());
			if (methodList == null)
			{
				methodList = new ArrayList<EndPointMethod>();
				classMap.put(classElement.toString(), methodList);
			}

			String returnType = methodElement.getReturnType().toString();
			String methoName = methodElement.getSimpleName().toString();
			
			
			EndPointMethod endPointMethod = new EndPointMethod(returnType, methoName);
			methodList.add(endPointMethod);
		
			List<? extends VariableElement> vElements = methodElement.getParameters();
			
			
			for (VariableElement vElement: vElements)
			{
				String name = vElement.toString();
				String type = vElement.asType().toString();
				
				endPointMethod.addArg(type, name);
			}

		}

		
		
		Set<String> classes = classMap.keySet();
		int count=0;
		for (String className : classes)
		{
			StringBuffer code = new StringBuffer(); 
			String _package = className.substring(0,className.lastIndexOf('.'));
			code.append("package "+_package+";\r\n");
			
			String _className = className.substring(className.lastIndexOf('.')+1)+"Impl";
			code.append("public class "+_className+" implements "+className+"\n");
			code.append("{\n");
			
			List<EndPointMethod> methods = classMap.get(className);
			for (EndPointMethod endPointMethod : methods)
			{
				code.append("\n\n");
				code.append(endPointMethod.getCode());
				code.append("\n\n");
				
			}
		
			
			code.append("}\n");
			
			System.out.println(code.toString());
			
		
			try
			{
				System.out.println(" ------("+count+++")----------- >"+_className);
				JavaFileObject  jfo = filer.createSourceFile(_className);
				jfo.delete();
				Writer writer = jfo.openWriter();
				writer.write(code.toString());
				writer.flush();
				writer.close();
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			
			
		}
		
		

	
		return true;
	}

}

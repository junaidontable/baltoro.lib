package io.baltoro.ep;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;



@SupportedAnnotationTypes({ "io.baltoro.ep.EndPoint" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class EndPointProcessor extends AbstractProcessor
{

	private Filer filer;
	private Messager messager;
	private List<FileObject> fileToDelete = new ArrayList<FileObject>();
	

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
		System.out.println(" ============================ ");
		System.out.println(" ============================ ");
		System.out.println(" ============================ ");
		
		return true;
	}
	
}

/*


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
			for (FileObject file : fileToDelete)
			{
				System.out.println("file to delete ..... "+file);
				boolean deleted = file.delete();
				System.out.println("file to delete ..... "+deleted);
				
			}
			
			return true;
		} 
		else
		{
			System.out.println("endPointElement = " + endPointElement);
		}

	
		
		Map<String, List<EPMethod>> classMap = new HashMap<String, List<EPMethod>>();
		Map<String, PackageElement> packageMap = new HashMap<String, PackageElement>();


		Set<? extends Element> methodElements = env.getElementsAnnotatedWith(endPointElement);

		for (Element _methodElement : methodElements)
		{
			ExecutableElement methodElement = (ExecutableElement) _methodElement;
			Element classElement = methodElement.getEnclosingElement();
			PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
			String _package = classElement.toString().substring(0,classElement.toString().lastIndexOf('.'));
			packageMap.put(_package, packageElement);
			
			List<EPMethod> methodList = classMap.get(classElement.toString());
			if (methodList == null)
			{
				methodList = new ArrayList<EPMethod>();
				classMap.put(classElement.toString(), methodList);
			}

			String returnType = methodElement.getReturnType().toString();
			String methoName = methodElement.getSimpleName().toString();
			
			
			EndPoint methodAnno = methodElement.getAnnotation(EndPoint.class);
			
			
			EPMethod endPointMethod = new EPMethod(returnType, methoName, methodAnno.appId(), methodAnno.path());
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
		for (String className : classes)
		{
			StringBuffer code = new StringBuffer(); 
			String _package = className.substring(0,className.lastIndexOf('.'));
			code.append("package "+_package+";\r\n\r\n");
			
			code.append("import io.baltoro.ep.*;\r\n");
			
			String _className = className.substring(className.lastIndexOf('.')+1)+"Impl";
			code.append("public class "+_className+" implements "+className+"\n");
			code.append("{\n");
			
			List<EPMethod> methods = classMap.get(className);
			for (EPMethod endPointMethod : methods)
			{
				code.append("\n\n");
				code.append(endPointMethod.getCode());
				code.append("\n\n");
				
			}
		
			
			code.append("}\n");
			
			System.out.println(code.toString());
			
		
			try
			{
				PackageElement packageElement = packageMap.get(_package);
						
				FileObject  fo = filer.getResource(StandardLocation.CLASS_OUTPUT, packageElement.toString(), _className);
				if(fo == null)
				{
					System.out.println(" file doesn't not exists");
				}
				else
				{
					System.out.println("file "+fo);
					fo.delete();
				}
				
				JavaFileObject  jfo = filer.createSourceFile(_className, packageElement);
				//jfo.delete();
				Writer writer = jfo.openWriter();
				writer.write(code.toString());
				writer.flush();
				writer.close();
				
				fileToDelete.add(jfo);
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			
			
		}
		
		

	
		return true;
	}

}
*/

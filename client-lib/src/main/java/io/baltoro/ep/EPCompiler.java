package io.baltoro.ep;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

final class EPCompiler
{
	private javax.tools.JavaCompiler tool;
	private StandardJavaFileManager stdManager;

	public EPCompiler()
	{
		tool = ToolProvider.getSystemJavaCompiler();
		if (tool == null)
		{
			throw new RuntimeException("Could not get Java compiler. Please, ensure that JDK is used instead of JRE.");
		}
		stdManager = tool.getStandardFileManager(null, null, null);
	}

	
	/**
	 * Compile a single static method.
	 */
	/*
	public Class compileStaticMethod(final String methodName, final String className, final String source)
			throws ClassNotFoundException
	{
		final Map<String, byte[]> classBytes = compile(className + ".java", source);
		final EPClassLoader classLoader = new EPClassLoader(classBytes);
		final Class clazz = classLoader.loadClass(className);
		return clazz;
		
		final Method[] methods = clazz.getDeclaredMethods();
		for (final Method method : methods)
		{
			if (method.getName().equals(methodName))
			{
				if (!method.isAccessible())
					method.setAccessible(true);
				return method;
			}
		}
		throw new NoSuchMethodError(methodName);
		
	}
	*/
	
	public Class<?> compileClass(String packageName, String className, final String source)
			throws ClassNotFoundException
	{
		final Map<String, byte[]> classBytes = compile(className + ".java", source);
		byte[] bytes = classBytes.get(packageName+"."+className);
		
		for(String key:classBytes.keySet())
		{
			System.err.println("key ===> "+key+", "+className);
		}
		
		System.out.println(bytes.length);
		
		//final EPClassLoader classLoader = new EPClassLoader(classBytes);
		
		
		
		final BytesClassLoader classLoader = new BytesClassLoader();
		Class<?> clazz = classLoader.loadThisClass(packageName+"."+className,bytes);
		
		
		//Class<?> clazz = Class.forName("io.baltoro.ep.TestEndpointCall1Impl");
		//Class<?> clazz = classLoader.loadClass(packageName+"."+className);
		
		System.out.println("class -- "+clazz);
		//clazz = Class.forName(packageName+"."+className);
		
		//this.getClass().getClassLoader().
		return clazz;
	}
	
	
	private Map<String, byte[]> compile(String fileName, String source)
	{
		return compile(fileName, source, new PrintWriter(System.err), null, null);
	}

	/**
	 * compile given String source and return bytecodes as a Map.
	 *
	 * @param fileName
	 *            source fileName to be used for error messages etc.
	 * @param source
	 *            Java source as String
	 * @param err
	 *            error writer where diagnostic messages are written
	 * @param sourcePath
	 *            location of additional .java source files
	 * @param classPath
	 *            location of additional .class files
	 */
	private Map<String, byte[]> compile(String fileName, String source, Writer err, String sourcePath, String classPath)
	{
		// to collect errors, warnings etc.
		//DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		// create a new memory JavaFileManager
		EPFileManager fileManager = new EPFileManager(stdManager);

		// prepare the compilation unit
		List<JavaFileObject> compUnits = new ArrayList<JavaFileObject>(1);
		compUnits.add(EPFileManager.makeStringSource(fileName, source));

		return compile(compUnits, fileManager, err, sourcePath, classPath);
	}

	private Map<String, byte[]> compile(final List<JavaFileObject> compUnits, final EPFileManager fileManager,
			Writer err, String sourcePath, String classPath)
	{
		// to collect errors, warnings etc.
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		// javac options
		List<String> options = new ArrayList<String>();
		options.add("-Xlint:all");
		// options.add("-g:none");
		options.add("-deprecation");
		if (sourcePath != null)
		{
			options.add("-sourcepath");
			options.add(sourcePath);
		}

		if (classPath != null)
		{
			options.add("-classpath");
			options.add(classPath);
		}

		// create a compilation task
		javax.tools.JavaCompiler.CompilationTask task = tool.getTask(err, fileManager, diagnostics, options, null,
				compUnits);

		if (task.call() == false)
		{
			PrintWriter perr = new PrintWriter(err);
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics())
			{
				perr.println(diagnostic);
			}
			perr.flush();
			return null;
		}

		Map<String, byte[]> classBytes = fileManager.getClassBytes();
		try
		{
			fileManager.close();
		} catch (IOException exp)
		{
		}

		return classBytes;
	}
}
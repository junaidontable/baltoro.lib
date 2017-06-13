package io.baltoro.ep;

/**
 * EPClassLoader that loads .class bytes from memory.
 */
final class EPClassLoader// extends URLClassLoader
{
	/*
	private Map<String, byte[]> classBytes;

	public EPClassLoader(Map<String, byte[]> classBytes, String classPath, EPClassLoader parent)
	{
		super(toURLs(classPath), parent);
		this.classBytes = classBytes;
	}

	public EPClassLoader(Map<String, byte[]> classBytes, String classPath)
	{
		super(classBytes, classPath, EPClassLoader.getSystemClassLoader());
	}

	public EPClassLoader(Map<String, byte[]> classBytes)
	{
		this(classBytes, null, EPClassLoader.getSystemClassLoader());
	}

	public Class<?> load(String className) throws ClassNotFoundException
	{
		return loadClass(className);
	}

	public Iterable<Class<?>> loadAll() throws ClassNotFoundException
	{
		List<Class<?>> classes = new ArrayList<Class<?>>(classBytes.size());
		for (String name : classBytes.keySet())
		{
			classes.add(loadClass(name));
		}
		return classes;
	}

	protected Class<?> findClass(String className) throws ClassNotFoundException
	{
		byte[] buf = classBytes.get(className);
		if (buf != null)
		{
			// clear the bytes in map -- we don't need it anymore
			classBytes.put(className, null);
			return defineClass(className, buf, 0, buf.length);
		} else
		{
			return super.findClass(className);
		}
	}

	private static URL[] toURLs(String classPath)
	{
		if (classPath == null)
		{
			return new URL[0];
		}

		List<URL> list = new ArrayList<URL>();
		StringTokenizer st = new StringTokenizer(classPath, LocalFile.pathSeparator);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			LocalFile file = new LocalFile(token);
			if (file.exists())
			{
				try
				{
					list.add(file.toURI().toURL());
				} catch (MalformedURLException mue)
				{
				}
			} else
			{
				try
				{
					list.add(new URL(token));
				} catch (MalformedURLException mue)
				{
				}
			}
		}
		URL[] res = new URL[list.size()];
		list.toArray(res);
		return res;
	}
	*/
}
package io.baltoro.client.compiler;
public class BytesClassLoader extends ClassLoader
{
  public Class<?> loadThisClass(String name, byte[] bytes)
  {
    resolveClass(defineClass(name,bytes, 0, bytes.length));
    Class<?> clazz = null;
    try
	{
		clazz = loadClass(name);
	} 
    catch (ClassNotFoundException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    return clazz;
  }
}
package io.baltoro.client;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.List;

public class LocalFileHelper
{

	static List<String> getDirectories(String rootPath)
	{
	
		 Path root = Paths.get(rootPath);
		 final List<String> paths=new ArrayList<>(2000);
		 try 
		 {
			 	
			 Files.walkFileTree(root, new SimpleFileVisitor<Path>()
			    {
					
					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
					{
						paths.add(dir.toString());
						return FileVisitResult.CONTINUE;
					}
				 
			    
			    });
		 } 
		 catch (IOException e) 
		 {
		      e.printStackTrace();
		 }
		 
		 return paths;
	}
}

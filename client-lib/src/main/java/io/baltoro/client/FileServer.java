package io.baltoro.client;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.baltoro.bto.RequestContext;

public class FileServer
{
	
	public static WebFile getFile(String dirPath, RequestContext req)
	{
		String path = null;
		
		if(req.getRelativePath() == null)
		{
			path = dirPath;
		}
		else if(dirPath.endsWith("/"))
		{
			path = dirPath+req.getRelativePath();
		}
		else
		{
			path = dirPath+"/"+req.getRelativePath();
		}
		
		
		File file = new File(path);
		
		System.out.println(" ---- > file : "+file.getName()+", "+file.length());
		try
		{
		
			WebFile webFile = new WebFile();
			webFile.file = file;
			
			Path filePath = Paths.get(path);
			byte[] data = Files.readAllBytes(filePath);
			webFile.data = data;
			return webFile;
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
		
	}

}

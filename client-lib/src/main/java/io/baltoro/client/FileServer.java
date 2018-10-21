package io.baltoro.client;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.baltoro.to.RequestContext;
import io.baltoro.to.ResponseContext;

public class FileServer
{
		
	public static WebFile getFile(String dirPath, RequestContext req, ResponseContext res)
	{
		String path = null;
		
		File file = new File(dirPath);
		if(file.isFile())
		{
			path = dirPath;
		}
		else
		{
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
			file = new File(path);
		}
		
		
		System.out.println(" loading ---- > file : "+file.getAbsolutePath()+", "+file.length());
		try
		{
		
			WebFile webFile = new WebFile();
			webFile.file = file;
			
			String extension = "";
					
					
			String fileName = file.getName();
			int i = fileName.lastIndexOf('.');
			if (i > 0) 
			{
			    extension = fileName.substring(i+1);
			}
			
			
			
			String contentType = MimeType.getMimeType(extension);
			webFile.contentType = contentType;
			res.setMimeType(contentType);
			res.setLastModifiedOn(file.lastModified());
			
			long browerTime = req.getIfModifiedSince();
			long fileTime = file.lastModified();
		
			if(fileTime == 0)
			{
				System.out.println(" ============== > file ("+file.getAbsolutePath()+") not found !!!");
				res.setError("file -> "+file.getName()+" not found .. ");
				webFile.error = "file -> "+file.getName()+" not found .. ";
				
				return webFile;
				
			}
			else if(browerTime >= fileTime)
			{
				res.setSendNotModified(true);
				return webFile;
			}
		
				
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

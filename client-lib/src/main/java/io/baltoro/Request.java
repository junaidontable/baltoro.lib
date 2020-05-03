package io.baltoro;

import java.util.List;
import java.util.Map;

public class Request
{

	public String tkn;
	public String reqUuid;
	public String clientUuid;
	public String cmd;
	public String url;
	public String uri;
	public String relativePath;
	public String appName;
	public String clientName;
	public String appUuid;
	public String method;
	
	public Map<String, String> headers;
	public Map<String, String> values;
	public Map<String, String> cookies;
	public List<UploadFile> uploadIds;

	
	
}

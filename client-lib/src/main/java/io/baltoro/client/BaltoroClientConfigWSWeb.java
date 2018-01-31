package io.baltoro.client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.HandshakeResponse;

public class BaltoroClientConfigWSWeb extends ClientEndpointConfig.Configurator
{
	
	static Logger log = Logger.getLogger(BaltoroClientConfigWSWeb.class.getName());
	
    static volatile boolean called = false;
    private String appName;
    private String token;
    private String path;
    
    public BaltoroClientConfigWSWeb(String appName, String path, String token)
	{
    	this.appName = appName;
		this.token = token;
		this.path = path;
	}

    @Override
    public void beforeRequest(Map<String, List<String>> headers) 
    {
        called = true;
        
       
        headers.put("BLT_APP_NAME", Arrays.asList(this.appName));
        headers.put("BLT_METHOD_PATH", Arrays.asList(this.path));
        headers.put("BLT_TOKEN", Arrays.asList(this.token));
       
    }

    @Override
    public void afterResponse(HandshakeResponse handshakeResponse) 
    {
        final Map<String, List<String>> headers = handshakeResponse.getHeaders();
    }
}
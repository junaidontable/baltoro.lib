package io.baltoro.client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.ws.rs.core.NewCookie;

public class BaltoroClientConfig extends ClientEndpointConfig.Configurator
{
	
	static Logger log = Logger.getLogger(BaltoroClientConfig.class.getName());
	
    static volatile boolean called = false;
    //private String appUuid;
    //private String instanceUuid;
    private Map<String, NewCookie> cookieMap;
    private String token;
    
   // public BaltoroClientConfig(Map<String, NewCookie> cookieMap, String appUuid, String insatnceUuid, String token)
    public BaltoroClientConfig(Map<String, NewCookie> cookieMap, String token)
	{
    	this.cookieMap = cookieMap;
		//this.appUuid = appUuid;
		//this.instanceUuid = insatnceUuid;
		this.token = token;
	}

    @Override
    public void beforeRequest(Map<String, List<String>> headers) 
    {
        called = true;
        
        for (String key : cookieMap.keySet())
		{
			NewCookie cookie = cookieMap.get(key);
			log.info("seinding WS +++++ >>>>>>>>>>> key["+key+"]:"+cookie);
			headers.put("Cookie", Arrays.asList(cookie.toString()));
		}	
        
       
        headers.put("BLT_APP_UUID", Arrays.asList(Baltoro.appUuid));
        headers.put("BLT_INSTANCE_UUID", Arrays.asList(Baltoro.instanceUuid));
        headers.put("BLT_TOKEN", Arrays.asList(this.token));
        headers.put("BLT_APP_NAME", Arrays.asList(Baltoro.appName));
        headers.put("BLT_SERVICE_NAME", Arrays.asList(Baltoro.serviceName));
        
        
       
    }

    @Override
    public void afterResponse(HandshakeResponse handshakeResponse) 
    {
        final Map<String, List<String>> headers = handshakeResponse.getHeaders();

        //assertEquals(HEADER_VALUE[0], headers.get(HEADER_NAME).get(0));
        //assertEquals(HEADER_VALUE[1], headers.get(HEADER_NAME).get(1));
        //assertEquals(HEADER_VALUE[2], headers.get(HEADER_NAME).get(2));
        //assertEquals("myOrigin", headers.get("origin").get(0));
    }
}
package io.baltoro.client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.ws.rs.core.NewCookie;

public class BaltoroClientConfigurator extends ClientEndpointConfig.Configurator
{
	
	static Logger log = Logger.getLogger(BaltoroClientConfigurator.class.getName());
	
    static volatile boolean called = false;
    //private String sessionId;
    private String appUuid;
    private String instanceUuid;
    private Map<String, NewCookie> cookieMap;
    private String token;
    
    public BaltoroClientConfigurator(Map<String, NewCookie> cookieMap, String appUuid, String insatnceUuid, String token)
	{
		//this.sessionId = sessionId;
    	this.cookieMap = cookieMap;
		this.appUuid = appUuid;
		this.instanceUuid = insatnceUuid;
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
        
        
        headers.put("BLT_APP_UUID", Arrays.asList(this.appUuid));
        headers.put("BLT_INSTANCE_UUID", Arrays.asList(this.instanceUuid));
        headers.put("BLT_TOKEN", Arrays.asList(this.token));
       
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
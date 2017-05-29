package io.baltoro.client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.HandshakeResponse;

public class BaltoroClientConfigurator extends ClientEndpointConfig.Configurator
{
    static volatile boolean called = false;
    private String sessionId;
    
    public BaltoroClientConfigurator(String sessionId)
	{
		this.sessionId = sessionId;
	}

    @Override
    public void beforeRequest(Map<String, List<String>> headers) 
    {
        called = true;
        headers.put("Cookie", Arrays.asList("JSESSIONID="+this.sessionId));
        headers.put("Origin", Arrays.asList("myOrigin"));
        System.out.println("**************"+this.sessionId+"***************");
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
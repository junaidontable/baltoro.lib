package io.baltoro;

import java.io.InputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;

import io.baltoro.util.Log;
import io.baltoro.util.StreamUtil;

public class WebClient
{

	static WebClient c;
	
	private Client client;
	private WebTarget wt;
	  
	
	private WebClient()
	{
		ClientConfig config = new ClientConfig();
        config.register(PollHeader.class);
        config.register(JacksonJsonProvider.class);
        config.property(ClientProperties.READ_TIMEOUT, 60000);
        config.property(ClientProperties.CONNECT_TIMEOUT, 5000);

 
		client = ClientBuilder.newClient(config);
		wt = client.target(Baltoro.POLL_URL);
		
		Log.log.finer(" ========================> URL = "+wt.getUri());

	}
	
	public static WebClient instance()
	{
		if(c == null)
		{
			c = new WebClient();
		}
		return c;
	}
	


	public String ping()
	{
		WebTarget t = this.wt.path("poll/ping");
		Invocation.Builder ib = t.request(MediaType.TEXT_HTML);
		
		String text = ib.get(String.class);
		
		return text;
	}
	
	public String poll() throws Exception
	{
		WebTarget t = this.wt.path("poll/poll");
	
		Invocation.Builder ib = t.request(MediaType.TEXT_HTML);
		
		String text = ib.get(String.class);
		return text;
	}
	
	public String response(io.baltoro.Response res)
	{
		
		
		WebTarget t = this.wt.path("poll/response");
				       
		Invocation.Builder ib =  t.request(MediaType.APPLICATION_JSON);
		
		
		Response response = ib.post(Entity.entity(res, MediaType.APPLICATION_JSON_TYPE));
		 
		String text = response.readEntity(String.class);
		return text;
	}
	
	
	public byte[] downloadFile(String fileUuid)
	{
		WebTarget t = this.wt.path("poll/downloadFile").queryParam("fileUuid", fileUuid);
		
		InputStream in = t.request().get(InputStream.class);
		byte[] bytes = StreamUtil.toBytes(in);
		return bytes;
	}
}

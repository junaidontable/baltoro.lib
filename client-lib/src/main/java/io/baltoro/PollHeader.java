package io.baltoro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

public class PollHeader implements ClientRequestFilter 
{
 
	static List<Object> apiKeyHeader;
	static List<Object> authCodeHeader;
	static List<Object> instUuidHeader;
	
	
	static
	{
		apiKeyHeader = new ArrayList<Object>();
		authCodeHeader = new ArrayList<Object>();
		instUuidHeader = new ArrayList<Object>();
		
		apiKeyHeader.add(Baltoro.API_KEY);
		authCodeHeader.add(Baltoro.AUTH_CODE);
		instUuidHeader.add(Baltoro.INST_UUID);
	}
	
    public void filter(ClientRequestContext rc) 
    throws IOException 
    {
        rc.getHeaders().put("api-key", apiKeyHeader);
        rc.getHeaders().put("auth-code", authCodeHeader);
        rc.getHeaders().put("inst-uuid", instUuidHeader);
        
       
        /*
        String tkn = System.currentTimeMillis()+"-"+Baltoro.AUTH_CODE;
        String eTkn = CryptoUtil.encrypt(Outpost.ENCODED_KEY, tkn.getBytes());
        
        List<Object> eTknHeader = new ArrayList<Object>();
        eTknHeader.add(eTkn);
        
        rc.getHeaders().put("auth-tkn", eTknHeader);
       */
    }
}
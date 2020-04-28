package io.baltoro.client;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.baltoro.client.util.CryptoUtil;
import io.baltoro.to.Keys;


public class TestKeyGen 
{
    

  
	//@Test
    public void testInsert()
    {
    	
    	
    	Keys keys = CryptoUtil.generateECKeys();
    
    
    	System.out.println(keys.getPrivateKey());
    	
    	System.out.println(keys.getPublicKey());
    	
    	
        assertTrue( true );
    }
    
}

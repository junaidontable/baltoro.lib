package io.baltoro.client;

import io.baltoro.client.util.CryptoUtil;
import io.baltoro.to.Keys;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TestKeyGen extends TestCase
{
    
    public TestKeyGen( String testName )
    {
        super( testName );
        
    }

    public static Test suite()
    {
        return new TestSuite( TestKeyGen.class );
    }
    
    
    public void testInsert()
    {
    	
    	
    	Keys keys = CryptoUtil.generateECKeys();
    
    
    	System.out.println(keys.getPrivateKey());
    	
    	System.out.println(keys.getPublicKey());
    	
    	
        assertTrue( true );
    }
    
}

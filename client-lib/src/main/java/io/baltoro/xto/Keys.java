package io.baltoro.xto;

import java.security.KeyPair;

public class Keys 
{
	private String privateKey;
	private String publicKey;

	public KeyPair keypair;
	
	
	public Keys(String privateKey, String publicKey) 
	{
		super();
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}
	
	

	public String getPrivateKey() 
	{
		return privateKey;
	}
	
	public void setPrivateKey(String privateKey) 
	{
		this.privateKey = privateKey;
	}
	
	public String getPublicKey() 
	{
		return publicKey;
	}
	
	public void setPublicKey(String publicKey) 
	{
		this.publicKey = publicKey;
	}
	

}

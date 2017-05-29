package io.baltoro.util;


import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;

import io.baltoro.to.Keys;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;



public class CryptoUtil
{
	private static String	digits = "0123456789abcdef";
	
	private static Logger log = Logger.getLogger(CryptoUtil.class.getName());
	 
	
	static
	{
		Security.addProvider(new BouncyCastleProvider());
	}
	
	
	public static String genAESKey() throws RuntimeException
	{
		try
		{
			KeyGenerator    generator = KeyGenerator.getInstance("AES", "BC");
		    generator.init(2048);
		    Key key = generator.generateKey();
		    
		    String str = Base64.getEncoder().encodeToString(key.getEncoded());
		    return str;
		} 
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		 
	       
	}
	
	
    public static Keys generateKeys()
    throws RuntimeException
    {

    	KeyPair pair = getKeyPair();
    
    	byte[] privateBytes = pair.getPrivate().getEncoded();
    	byte[] publicBytes = pair.getPublic().getEncoded();
    	
    	String privateKey = Base64.getEncoder().encodeToString(privateBytes);
    	String publicKey = Base64.getEncoder().encodeToString(publicBytes);
    	
    	Keys keys = new Keys(privateKey, publicKey);
    	keys.keypair = pair;
    	
	    return keys;
    }
    
    private static KeyPair getKeyPair() 
    throws RuntimeException
    {
    	KeyPairGenerator gen = null;
		
    	try 
    	{
			gen = KeyPairGenerator.getInstance("RSA", "BC");
		} 
    	catch (Exception e) 
    	{
			throw new RuntimeException(e);
		} 
    	
    	SecureRandom random = null;
    	try
    	{
    		random = new SecureRandom();//.getInstance("SHA1PRNG", "SUN");
    	}
    	catch(Exception e)
    	{
    		random = new SecureRandom();
    	}
    	
    	gen.initialize(2048, random);

    	KeyPair pair = gen.generateKeyPair();   
    	
    	return pair;
    }
    
	public static String encrypt(String encodedKey, String encodedInput) 
	throws RuntimeException
	{
		try 
		{
			byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
			int size = keyBytes.length;
			//SecretKey key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "RSA");
			
			Key key = null;
			
			
			if(size < 600)
			{
				key =  KeyFactory.getInstance("RSA","BC").generatePublic(new X509EncodedKeySpec(keyBytes));
			}
			else
			{
				//key = new PrivateKeyFactory().
				 PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
			     KeyFactory rsaFact = KeyFactory.getInstance("RSA","BC");
			     RSAPrivateKey _pk = (RSAPrivateKey) rsaFact.generatePrivate(spec);
			       
			     key = _pk;
			}
			
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding","BC");
			//Cipher cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			
			byte[] bytes = Base64.getDecoder().decode(encodedInput);
			byte[] encBytes = cipher.doFinal(bytes);
			
			String encStr = Base64.getEncoder().encodeToString(encBytes);
			return encStr;
		} 
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	
	public static String encryptWithPassword(String password, String input) 
	throws RuntimeException
	{
		try 
		{
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), "fat".getBytes(), 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secret);

			//byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
			byte[] ciphertext = cipher.doFinal(input.getBytes());
			String encStr = Base64.getEncoder().encodeToString(ciphertext);
			return encStr;

		} 
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	public static String decryptWithPassword(String password, String input) 
	throws RuntimeException
	{
		try 
		{
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), "fat".getBytes(), 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secret);

			//byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
			byte[] inbytes = Base64.getDecoder().decode(input.getBytes());
			byte[] ciphertext = cipher.doFinal(inbytes);
			return new String(ciphertext);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static String decrypt(String encodedKey, String encodedInput) 
	throws RuntimeException
	{
		try 
		{
			byte[] keyBytes = StringUtil.decode(encodedKey);
			int size = keyBytes.length;
			Key key = null;
			
			if(size < 600)
			{
				key =  KeyFactory.getInstance("RSA","BC").generatePublic(new X509EncodedKeySpec(keyBytes));
			}
			else
			{
				PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
			    KeyFactory rsaFact = KeyFactory.getInstance("RSA","BC");
			    RSAPrivateKey _pk = (RSAPrivateKey) rsaFact.generatePrivate(spec);
			    key = _pk;
			}
			
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding","BC");
			cipher.init(Cipher.DECRYPT_MODE, key);
			
			byte[] _input = Base64.getDecoder().decode(encodedInput);
			byte[] bytes = cipher.doFinal(_input);
			
			String encodedStr =  StringUtil.encode(bytes);
			return encodedStr;
		} 
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	
	


	public static String hash(String str) 
	throws RuntimeException 
	{
		try 
		{
			byte[] bytes = MessageDigest.getInstance("SHA-256").digest(str.getBytes());
			return Base64.getEncoder().encodeToString(bytes);
		} 
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
		
	}
	
	public static String hash(byte[] contentBytes) 
	throws RuntimeException 
	{
		try 
		{
			byte[] bytes = MessageDigest.getInstance("SHA-256").digest(contentBytes);
			return Base64.getEncoder().encodeToString(bytes);
		} 
		catch (Exception e) 
		{
			throw new RuntimeException(e);
		}
		
	}
	
	private static byte[] checkPadding(byte[] input)
	{
		
		
		if(input.length >= 16)
			return input;
		
		
		byte[] _input = new byte[16];
		int i = 0;
		for (; i < input.length; i++)
		{
			_input[i] = input[i];
		}
		
		for(;i<_input.length;i++)
		{
			_input[i] = 20;
		}
		
		return _input;
		
	}

	
	public static X509Certificate signCert(KeyPair pair, String name, X509Certificate caCert, PrivateKey caKey)
	throws Exception
	{
	  
		/*
		String sigAlg = "SHA1WithRSAEncryption";
        PKCS10 pkcs10 = new PKCS10(pair.getPublic());
        Signature signature = Signature.getInstance(sigAlg);
        signature.initSign(pair.getPrivate());
        X500Name x500Name = new X500Name("baltoro", name, "ca", "Palo Alto","California", "USA");
        pkcs10.encodeAndSign(x500Name,signature);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bs);
        pkcs10.print(ps);
        byte[] csrbytes = bs.toByteArray();
        
        CertificateFactory certFact = CertificateFactory.getInstance("X.509"); 
        
        //certFact.generateCertificate(inStream);
        */
		
        X509V3CertificateGenerator  certGen = new X509V3CertificateGenerator();

        certGen.setSerialNumber(BigInteger.valueOf(1));
        certGen.setIssuerDN(caCert.getSubjectX500Principal());
        certGen.setNotBefore(new Date(System.currentTimeMillis()));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 10000000));
        certGen.setSubjectDN(new X500Principal("CN=baltoro,OU="+name));
        certGen.setPublicKey(pair.getPublic());
        certGen.setSignatureAlgorithm("SHA1WithRSA");
        
        certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
        //certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(entityKey));
        certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
        certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        
        X509Certificate cert = certGen.generateX509Certificate(pair.getPrivate(), "BC");
        
        
        return cert;
        /*
        X509V1CertificateGenerator  certGen = new X509V1CertificateGenerator();

        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setIssuerDN(new X500Principal("CN=baltoro,OU="+name));
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 50000));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 50000));
        certGen.setSubjectDN(new X500Principal("CN=Test Certificate"));
        certGen.setPublicKey(pair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

        
        return cert; 
        */
        //InputStream  bis  = new ByteArrayInputStream(csrbytes);
       // javax.security.cert.Certificate cert = certFact.generateCertificate(bis); 
	   
        
		
	} 
	

	public static X509Certificate genCert(String name)
	throws Exception
	{
	  
		CertAndKeyGen certGen = new CertAndKeyGen("RSA","SHA1WithRSA",null);
		certGen.generate(1024);
		
        X509Certificate cert = certGen.getSelfCertificate(new X500Name("CN=Baltoro.com"), (long)365*24*3600);
        
		return cert;
	}   

    
	public static String toHex(byte[] data, int length)
    {
        StringBuffer	buf = new StringBuffer();
        
        for (int i = 0; i != length; i++)
        {
            int	v = data[i] & 0xff;
            
            buf.append(digits.charAt(v >> 4));
            buf.append(digits.charAt(v & 0xf));
        }
        
        return buf.toString();
    }
	
	public static String toHex(byte[] data)
    {
        return toHex(data, data.length);
    }
	
	public static String makeBlankString(int len)
    {
        char[]   buf = new char[len];
        
        for (int i = 0; i != buf.length; i++)
        {
            buf[i] = ' ';
        }
        
        return new String(buf);
    }
	
	/*
	public static void sign(ReqTO to, String privateKey)
	{
		StringBuilder json = new StringBuilder(2000);
		json.append("{\"processor-data\":{ ");
		for (ReqTOField field : to.getFieldList())
		{
			json.append("\""+field+"\"");
			json.append(':');
			json.append("\""+to.getFieldValueMap().get(field)+"\"");
			json.append(",");
		}
		json.delete(json.length()-1,json.length());
		
		if(!to.getTxUuidList().isEmpty())
		{
			json.append(",\"tx\":[\n");
			for (String txUuid : to.getTxUuidList())
			{
				json.append("{\"uuid\":");
				json.append("\""+txUuid+"\"");
				json.append(",");
				json.append("\"tx-hash\":");
				json.append("\""+to.getTxHashMap().get(txUuid)+"\"");
				json.append("}\n");
			}
			json.append("]\n");
		}
		
		json.append("}}");
		
		to.setJson(json.toString());
		String hash = hash(to.getJson());
		to.setHash(hash);
		String encHash = encrypt(privateKey, hash);
		to.setSignature(encHash);
	}
	*/
}

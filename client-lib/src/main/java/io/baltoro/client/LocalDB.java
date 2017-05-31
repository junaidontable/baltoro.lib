package io.baltoro.client;

class LocalDB
{

	//private String framework = "embedded";
	
	/*
	private String protocol = "jdbc:derby:";

	private String dbName;
	private Connection con;
	private Baltoro client;
	private Map<OName,String> map;
	Baltoro baltoro;
	
	public LocalDB(Baltoro baltoro)
	{
		this.baltoro = baltoro;
		this.dbName = "baltoro-db";
		
		try
		{
			initLocalDB();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	private void initLocalDB()
	throws Exception
	{
		con = DriverManager.getConnection(protocol + dbName + ";create=true");
		con.setAutoCommit(true);
		try
		{
			map = get(OTypes.USER);
		} 
		catch (SQLException e)
		{
			System.out.println("setting up local database.... ");
			setupTables();
		}
		
	}
	

	void cleanUp() throws Exception
	{
		deleteTables();
	}
	
	void deleteTables() throws Exception
	{
		Statement st = con.createStatement();
		
		st = con.createStatement();
		st.execute("drop table base");
		st.close();
		
	}
	
	private void setupTables() throws Exception
	{
		Statement st = con.createStatement();
		st.execute("create table base(type varchar(10) not null, name varchar(256) not null, "
				+ "value varchar(9000), created_on timestamp default current_timestamp, "
				+ "primary key (type,name))");
		st.close();
		
		
		System.out.println("Created tables");
	}
	
	void save(OName name, String value)
	throws Exception
	{
		PreparedStatement st = con.prepareStatement("insert into base(type,name,value) values(?,?,?)");
		st.setString(1, OTypes.USER.toString());
		st.setString(2, name.toString());
		st.setString(3, value);
		try
		{
			st.executeUpdate();
		} 
		catch (SQLIntegrityConstraintViolationException e)
		{
			//System.out.println("record already exists, updating ..");
			update(name, value);
		}
		st.close();
	}
	
	void update(OName name, String value)
	throws Exception
	{
		PreparedStatement st = con.prepareStatement("update base set value = ? where type=? and name=?");
		st.setString(1, value);
		st.setString(2, OTypes.USER.toString());
		st.setString(3, name.toString());
		st.executeUpdate();
		st.close();
	}
	
	
	private Map<OName, String> get(OTypes type) throws Exception
	{
		PreparedStatement st = con.prepareStatement("select name,value from base where type=?");
		st.setString(1, type.toString());
		
		Map<OName, String> map = new HashMap<OName,String>();
		
		ResultSet rs = st.executeQuery();
		while(rs.next())
		{
			String name = rs.getString("name");
			String value = rs.getString("value");
			
			map.put(OName.valueOf(name), value);		
		}
		rs.close();
		st.close();
		
		return map;
	}
	
	String get(OName name) throws Exception
	{
		PreparedStatement st = con.prepareStatement("select value from base where type=? and name=?");
		st.setString(1, OTypes.USER.toString());
		st.setString(2, name.toString());
		
		ResultSet rs = st.executeQuery();
		String value = null;
		if(rs.next())
		{
			value = rs.getString(1);
		}
		rs.close();
		st.close();
		
		return value;
	}
	
	boolean isSetup()throws Exception
	{
		if(map == null || map.isEmpty())
		{
			return false;
		}
		else
		{
			return true;
		}
		
	}
	
	private Map<OName,String> getMap()
	{
		return this.map;
	}
	
	
	
	void setup(UserTO user,String password) throws Exception
	{
		save(OName.USER_UUID, user.uuid);
		save(OName.EMAIL, user.email);
		
		String salt = UUIDGenerator.randomString(5);
	    String saveHash = CryptoUtil.hash(salt+password);
	    
	    save(OName.PASSWORD_HASH, saveHash);
	    save(OName.PASSWORD_SALT, salt);
	    
	    //save(OName.PUBLIC_KEY, client.publicKey);
		//String encPrivateKey = CryptoUtil.encryptWithPassword(password, client.privateKey);
		//save(OName.PRIVATE_KEY, encPrivateKey);
	  
		map = get(OTypes.USER);
		
		System.out.println("setup complete");
	}
	
	String login(String password) throws Exception
	{
		String pHash = map.get(OName.PASSWORD_HASH);
		String salt = map.get(OName.PASSWORD_SALT);
		String hash = CryptoUtil.hash(salt+password);
		String email = map.get(OName.EMAIL);   
		
	    if(pHash.equals(hash))
	    {
	    	System.out.println("success local password matchs");
	    	//String _privateKey = map.get(OName.PRIVATE_KEY);
	    	//String privateKey = CryptoUtil.decryptWithPassword(password, _privateKey);
	    	//client.privateKey = privateKey;
	    	//client.publicKey = map.get(OName.PUBLIC_KEY);
	    	return email;
	    }
	    else
	    {
	    	//System.out.println("not");
	    	return null;
	    }
		
		
	}

*/

}

package io.baltoro.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import io.baltoro.client.util.StringUtil;
import io.baltoro.client.util.UUIDGenerator;
import io.baltoro.domain.BODefaults;
import io.baltoro.obj.Base;


public class LocalDB
{

	//private String framework = "embedded";
	
	private static LocalDB db;
	private String protocol = "jdbc:derby:";

	private String instUuid;
	private Connection con;
	
	
	public static LocalDB instance()
	{
		if(db == null)
		{
			db = new LocalDB(Baltoro.instanceUuid);
		}
		return db;
	}
	
	private LocalDB(String instUuid)
	{
		this.instUuid = instUuid;
		
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
		con = DriverManager.getConnection(protocol + instUuid + ";create=true");
		con.setAutoCommit(true);
		
		try
		{
			cleanUp();
			con.createStatement().executeQuery("select uuid from object_base WHERE uuid='1'");
		} 
		catch (SQLException e)
		{
			System.out.println("setting up local database.... "+e);
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
		
		st = con.createStatement();
		st.execute("drop table version");
		st.close();
		
		st = con.createStatement();
		st.execute("drop table metadata");
		st.close();
		
		st = con.createStatement();
		st.execute("drop table link");
		st.close();
		
		
		st = con.createStatement();
		st.execute("drop table permission");
		st.close();
		
		st = con.createStatement();
		st.execute("drop table type");
		st.close();
	}
	
	private void setupTables() throws Exception
	{
		Statement st = con.createStatement();
		
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE TABLE base (");
		sql.append("uuid varchar(42) NOT NULL,");
		sql.append("name varchar(256) NOT NULL,");
		sql.append("state varchar(8) NOT NULL,");
		sql.append("type varchar(5) NOT NULL,");
		sql.append("container_uuid varchar(42) NOT NULL,");
		sql.append("latest_version_uuid varchar(42) NOT NULL,");
		sql.append("latest_version_number smallint NOT NULL,");
		sql.append("permission_type varchar(4) NOT NULL,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (uuid))");
		System.out.println(sql.toString());
		st.execute(sql.toString());
		st.close();
		
		
		
		createIndex("base", "name");
		createIndex("base", "created_on");
		createIndex("base", "container_uuid");
		createIndex("base", "type");
		createIndex("base", "name,container_uuid,type");
		
		System.out.println("Base Table Created");
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE version (");
		sql.append("uuid varchar(42) NOT NULL,");
		sql.append("base_uuid varchar(42) NOT NULL,");
		sql.append("version_number smallint NOT NULL,");
		sql.append("name varchar(256) NOT NULL,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (uuid))");
		System.out.println(sql.toString());
		st = con.createStatement();
		st.execute(sql.toString());
		st.close();
		
		createIndex("version", "name");
		createIndex("version", "base_uuid");
		createIndex("version", "created_on");
		
		System.out.println("Version Table Created");
		
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE metadata (");
		sql.append("uuid varchar(42) NOT NULL,");
		sql.append("base_uuid varchar(42) NOT NULL,");
		sql.append("version_uuid varchar(42) NOT NULL,");
		sql.append("name varchar(256) NOT NULL,");
		sql.append("value varchar(32672) NOT NULL,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (uuid))");
		System.out.println(sql.toString());
		st = con.createStatement();
		st.execute(sql.toString());
		st.close();
		
		createIndex("metadata", "name");
		createIndex("metadata", "base_uuid");
		createIndex("metadata", "version_uuid");
		
		System.out.println("Metadata Table Created");
		
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE link (");
		sql.append("uuid varchar(42) NOT NULL,");
		sql.append("p_uuid varchar(42) NOT NULL,");
		sql.append("c_uuid varchar(42) NOT NULL,");
		sql.append("ctx_uuid varchar(42) NOT NULL,");
		sql.append("sort smallint NOT NULL DEFAULT 1,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (uuid))");
		System.out.println(sql.toString());
		st = con.createStatement();
		st.execute(sql.toString());
		st.close();
		
		createIndex("link", "p_uuid");
		createIndex("link", "c_uuid");
		createIndex("link", "created_on");
		
		System.out.println("Link Table Created");
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE permission (");
		sql.append("uuid varchar(42) NOT NULL,");
		sql.append("base_uuid varchar(42) NOT NULL,");
		sql.append("ctx_uuid varchar(42) NOT NULL,");
		sql.append("perm_read smallint NOT NULL DEFAULT 0,");
		sql.append("perm_edit smallint NOT NULL DEFAULT 0,");
		sql.append("perm_delete smallint NOT NULL DEFAULT 0,");
		sql.append("perm_link smallint NOT NULL DEFAULT 0,");
		sql.append("perm_grantt smallint NOT NULL DEFAULT 0,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (uuid))");
		System.out.println(sql.toString());
		st = con.createStatement();
		st.execute(sql.toString());
		st.close();
		
		createIndex("permission", "base_uuid");
		createIndex("permission", "ctx_uuid");
		createIndex("permission", "created_on");
		
		System.out.println("Permission Table Created");
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE type (");
		sql.append("class varchar(2000) NOT NULL,");
		sql.append("type varchar(5) NOT NULL,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (class))");
		System.out.println(sql.toString());
		st = con.createStatement();
		st.execute(sql.toString());
		st.close();
		
		createIndex("type", "type");
		
		System.out.println("type Table Created");
		
	}
	
	
	private void createIndex(String tableName, String cols)
	throws Exception
	{
		Statement st = con.createStatement();
		String indexName = UUIDGenerator.randomString(6);
	
		String sql = "CREATE INDEX IDX_"+tableName+"_"+indexName.toUpperCase()+" on "+tableName+"("+cols+")";
		System.out.println(sql);
		st.execute(sql);
		st.close();
	}
	
	public void save(Base obj)
	{
		if(StringUtil.isNullOrEmpty(obj.getBaseUuid()))
		{
			String type = getType(obj);
			obj.setType(type);
			String uuid = UUIDGenerator.uuid(type);
			String versionUuid = UUIDGenerator.uuid(type);
			obj.setBaseUuid(uuid);
			obj.setVersionNumber(1);
			obj.setLatestVersionUuid(versionUuid);
			obj.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			
			insertBase(obj);
			insertVersion(obj);
		}
		else
		{
			int vn = obj.getVersionNumber();
			vn++;
			obj.setVersionNumber(vn);
			
			String versionUuid = UUIDGenerator.uuid(obj.getType());
			obj.setLatestVersionUuid(versionUuid);
			updateBase(obj);
			insertVersion(obj);
		}
	}
	
	
	
	private void insertBase(Base obj)
	{
		PreparedStatement st = null;
		try
		{
			st = con.prepareStatement("insert into base(uuid, name, state, type, container_uuid, latest_version_uuid, "
							+ "latest_version_number, permission_type, created_by, created_on) "
					+ "values(?,?,?,?,?,?,?,?,?,?)");
			
			st.setString(1, obj.getBaseUuid());
			st.setString(2, obj.getName());
			st.setString(3, obj.getState());
			st.setString(4, obj.getType());
			st.setString(5, obj.getContainerUuid());
			st.setString(6, obj.getLatestVersionUuid());
			st.setInt(7, obj.getVersionNumber());
			st.setString(8, obj.getPermissionType());
			st.setString(9, obj.getCreatedBy());
			st.setTimestamp(10, obj.getCreatedOn());
			
			//System.out.println(st.get);
			st.execute();
			st.close();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private void updateBase(Base obj)
	{
		PreparedStatement st = null;
		try
		{
			st = con.prepareStatement("update base set name=?, latest_version_uuid =?, "
							+ "latest_version_number=? where uuid=? ");
				
			
			st.setString(1, obj.getName());
			st.setString(2, obj.getLatestVersionUuid());
			st.setInt(3, obj.getVersionNumber());
			st.setString(4, obj.getBaseUuid());
			
			st.executeUpdate();
			st.close();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private void insertVersion(Base obj)
	{
		
		
		PreparedStatement st = null;
		try
		{
			/*
			sql.append("uuid varchar(42) NOT NULL,");
			sql.append("base_uuid varchar(42) NOT NULL,");
			sql.append("version_number smallint NOT NULL,");
			sql.append("name varchar(256) NOT NULL,");
			sql.append("created_by varchar(42) NOT NULL, ");
			sql.append("created_on timestamp NOT NULL,");
			*/
			
			st = con.prepareStatement("insert into version(uuid, base_uuid, version_number, name, created_by, created_on) "
					+ "values(?,?,?,?,?,?)");
			
			st.setString(1, obj.getLatestVersionUuid());
			st.setString(2, obj.getBaseUuid());
			st.setInt(3, obj.getVersionNumber());
			st.setString(4, obj.getName());
			st.setString(5, obj.getCreatedBy());
			st.setTimestamp(6, obj.getCreatedOn());
			
			st.execute();
			st.close();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private String getType(Base obj)
	{
		String type = null;
		
		
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			st = con.prepareStatement("select * from type where class = ?");
			st.setString(1, obj.getClass().getName());
			rs = st.executeQuery();
			if(rs.next())
			{
				type = rs.getString("type");
			}
			
			if(type != null)
			{
				return type;
			}
			
			st.close();
			st = con.prepareStatement("insert into type(class,type,created_by, created_on) values (?,?,?,?)");
			st.setString(1, obj.getClass().getName());
			type = UUIDGenerator.randomString(4);
			st.setString(2, type);
			st.setString(3, BODefaults.BASE_USER);
			st.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			st.execute();
			rs.close();
			st.close();
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return type;
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


}

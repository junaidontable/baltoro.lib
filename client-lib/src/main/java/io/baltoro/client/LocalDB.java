package io.baltoro.client;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.BatchUpdateException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.derby.impl.jdbc.EmbedConnection;
import org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.client.util.CryptoUtil;
import io.baltoro.client.util.ObjectUtil;
import io.baltoro.client.util.StringUtil;
import io.baltoro.client.util.UUIDGenerator;
import io.baltoro.exp.LocalDBException;
import io.baltoro.features.Store;
import io.baltoro.obj.BODefaults;
import io.baltoro.obj.Base;
import io.baltoro.to.ReplicationTO;


public class LocalDB
{

	//private String framework = "embedded";
	private ObjectMapper mapper = new ObjectMapper();
	private static LocalDB db;

	private String protocol = "jdbc:derby:";

	private String instUuid;
	//private Connection con;
	private static String dbName;
	
	static ConcurrentLinkedQueue<Connection> connectionQueue = new ConcurrentLinkedQueue<>();
	
	//static boolean initPull = false;
	
	
	Map<String, String> typeClassMap = new HashMap<>(100);
	Map<String, String> classTypeMap = new HashMap<>(100);
	
	Map<String, MDFieldMap> classFieldMap = new HashMap<>(1000);
	
	public static LocalDBBinary binary;
	static private Timer freeCon;
	
	
	static LocalDB instance()
	{
		
		if(db == null)
		{
			synchronized ("db-init".intern())
			{
				if(db == null)
				{
			
					//String dbName = "LDB-"+Baltoro.appName+"-"+Baltoro.serviceNames.toString().replaceAll(",", "-").replaceAll("/", "-");
					String serviceName = Baltoro.serviceNames.toString().replaceAll(",","");
					
					String homeDir = System.getProperty("user.home");
					String bltDir = homeDir+"/baltoro.io";
			    	File f = new File(bltDir);
			    	if(!f.exists())
			    	{
			    		f.mkdirs();
			    	}
					
					//String _dbName = "LDB-"+Baltoro.appName+"-"+serviceName+"-"+Baltoro.hostId;
					
					dbName = bltDir+"/LDB-"+Baltoro.appName+"-"+serviceName+"-"+Baltoro.hostId;
					
					System.out.println("[[[[[[[[[[[ local db name = "+dbName+" ]]]]]]]]]]]]]]]");
					db = new LocalDB(dbName);
					
					if(Baltoro.env == Env.JUNIT)
					{
						System.out.println(" ........ cleaned local db ... for junit tests ");
					}
					else
					{
						Replicator.start();
					}
				}
			}
		}
		return db;
	}
	

	
	private LocalDB(String instUuid)
	{
		this.instUuid = instUuid;
		
		freeCon = new Timer();
		freeCon.schedule(new TimerTask()
		{
			
			@Override
			public void run()
			{
				System.out.println("free local db connections =====> "+connectionQueue.size()+", to change call Baltoro.setDBConnectionPoolSize(int size) ");
			}
		}, 1000, 10000);
		
		
		try
		{
			initLocalDB();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public LocalDBBinary getBinary()
	{
		return binary;
	}
	
	private void initLocalDB()
	throws Exception
	{
		
		try
		{
			DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
			
			System.out.println("init connection pool =====> "+Baltoro.dbConnectionPoolSize);
			for (int i = 0; i < Baltoro.dbConnectionPoolSize; i++)
			{
				EmbedConnection _con = (EmbedConnection)DriverManager.getConnection(protocol + instUuid + ";create=true");
				_con.setAutoCommit(true);
				Connection con = new Connection(_con);
				connectionQueue.add(con);
			}
			
		} 
		catch (SQLException e)
		{
			System.err.println(" *************************************** ");
			System.err.println(" *************************************** ");
			System.err.println(" ANOTHER INSTANCE IS USING THE LOCAL DB ");
	
			System.err.println(" *************************************** ");
			System.err.println(" *************************************** ");
			
			e.printStackTrace();
			
			System.exit(1);
		}
		
		Connection con = getConnection();
		try
		{
	
			con.createStatement().executeQuery("select uuid from base WHERE uuid='1'");
			
			System.out.println("Found local database.... "+dbName);
		} 
		catch (SQLException e)
		{
			System.out.println("setting up NEW local database.... "+dbName);
		
			setupTables(con);
			
		}
		finally 
		{
			con.close();
		}
		
				
	}
	
	
	
	
	private Connection getConnection()
	{
		Connection con = connectionQueue.poll();
		if(con == null)
		{
			throw new RuntimeException("no connection avialbale ....");
		}
		
		return con;
	}

	void cleanUp() throws Exception
	{
		deleteTables();
	}
	
	
	void cleanData()
	{
		Connection con = getConnection();
		try
		{
			Statement st = con.createStatement();
			
			st = con.createStatement();
			st.executeNoReplication("delete from base");
			st.close();
			
			st = con.createStatement();
			st.executeNoReplication("delete from version");
			st.close();
			
			st = con.createStatement();
			st.executeNoReplication("delete from metadata");
			st.close();
			
			st = con.createStatement();
			st.executeNoReplication("delete from link");
			st.close();
			
			st = con.createStatement();
			st.executeNoReplication("delete from link_att");
			st.close();
			
			
			st = con.createStatement();
			st.executeNoReplication("delete from permission");
			st.close();
			
			
			st = con.createStatement();
			st.executeNoReplication("delete from type");
			st.close();
			
			
			/*
			st = con.createStatement();
			st.execute("drop table binary", null);
			st.close();
			*/
			
			st = con.createStatement();
			st.executeNoReplication("delete from repl_pull");
			st.close();
			
			st = con.createStatement();
			st.executeNoReplication("delete from repl_push");
			st.close();
			
			st = con.createStatement();
			st.executeNoReplication("delete from content");
			st.close();
		
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
	}
	
	void deleteTables() throws Exception
	{
		Connection con = getConnection();
		
		Statement st = con.createStatement();
		
		st = con.createStatement();
		st.executeNoReplication("drop table base");
		st.close();
		
		st = con.createStatement();
		st.executeNoReplication("drop table version");
		st.close();
		
		st = con.createStatement();
		st.executeNoReplication("drop table metadata");
		st.close();
		
		st = con.createStatement();
		st.executeNoReplication("drop table link");
		st.close();
		
		st = con.createStatement();
		st.executeNoReplication("drop table link_att");
		st.close();
		
		
		st = con.createStatement();
		st.executeNoReplication("drop table permission");
		st.close();
		
		
		st = con.createStatement();
		st.executeNoReplication("drop table type");
		st.close();
		
		
		/*
		st = con.createStatement();
		st.execute("drop table binary", null);
		st.close();
		*/
		
		st = con.createStatement();
		st.executeNoReplication("drop table repl_pull");
		st.close();
		
		st = con.createStatement();
		st.executeNoReplication("drop table repl_push");
		st.close();
		
		st = con.createStatement();
		st.executeNoReplication("drop table content");
		st.close();
		
		con.close();
	}
	
	private void setupTables(Connection con) throws Exception
	{
		
		Statement st = con.createStatement();
		
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE TABLE base (");
		sql.append("uuid varchar(42) NOT NULL,");
		sql.append("name varchar(32672) NOT NULL,");
		sql.append("state varchar(8) NOT NULL,");
		sql.append("type varchar(5) NOT NULL,");
		sql.append("container_uuid varchar(42) NOT NULL,");
		sql.append("latest_version_uuid varchar(42) NOT NULL,");
		sql.append("latest_version_number smallint NOT NULL,");
		sql.append("permission_type varchar(4) NOT NULL,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (uuid))");
		//System.out.println(sql.toString());
		st.executeNoReplication(sql.toString());
		st.close();
		
		createIndex("base", "name", con);
		createIndex("base", "created_on", con);
		createIndex("base", "container_uuid", con);
		createIndex("base", "type", con);
		createIndex("base", "latest_version_uuid", con);
		createIndex("base", "name,container_uuid,type", con);
		
		System.out.println("Base Table Created");
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE version (");
		sql.append("uuid varchar(42) NOT NULL,");
		sql.append("base_uuid varchar(42) NOT NULL,");
		sql.append("version_number smallint NOT NULL,");
		sql.append("name varchar(32672) NOT NULL,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (uuid))");
		//System.out.println(sql.toString());
		st = con.createStatement();
		st.executeNoReplication(sql.toString());
		st.close();
		
		createIndex("version", "name", con);
		createIndex("version", "base_uuid", con);
		createIndex("version", "created_on", con);
		
		System.out.println("Version Table Created");
		
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE metadata (");
		sql.append("base_uuid varchar(42) NOT NULL,");
		sql.append("version_uuid varchar(42) NOT NULL,");
		sql.append("name varchar(256) NOT NULL,");
		sql.append("value varchar(32672) NOT NULL,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (base_uuid,version_uuid,name))");
		//System.out.println(sql.toString());
		st = con.createStatement();
		st.executeNoReplication(sql.toString());
		st.close();
		
		createIndex("metadata", "name", con);
		createIndex("metadata", "base_uuid", con);
		createIndex("metadata", "version_uuid", con);
		
		System.out.println("Metadata Table Created");
		
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE link (");
		sql.append("uuid varchar(42) NOT NULL,");
		sql.append("p_uuid varchar(42) NOT NULL,");
		sql.append("c_uuid varchar(42) NOT NULL,");
		sql.append("p_obj_type varchar(5) NOT NULL,");
		sql.append("c_obj_type varchar(5) NOT NULL,");
		sql.append("sort smallint NOT NULL DEFAULT 50,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (uuid))");
		
		st = con.createStatement();
		st.executeNoReplication(sql.toString());
		st.close();
		
		createIndex("link", "p_uuid", con);
		createIndex("link", "c_uuid", con);
		createIndex("link", "p_obj_type", con);
		createIndex("link", "c_obj_type", con);
		createIndex("link", "created_on", con);
		
		System.out.println("Link Table Created");
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE link_att (");
		sql.append("uuid varchar(42) NOT NULL,");
		sql.append("link_uuid varchar(42) NOT NULL,");
		sql.append("name varchar(64) NOT NULL,");
		sql.append("value varchar(256) NOT NULL,");
		sql.append("PRIMARY KEY (uuid))");
		
		st = con.createStatement();
		st.executeNoReplication(sql.toString());
		st.close();
		
		createIndex("link_att", "link_uuid", con);
		createIndex("link_att", "name", con);
		createIndex("link_att", "value", con);
		
		System.out.println("Link_att Table Created");
		
		
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
		//System.out.println(sql.toString());
		st = con.createStatement();
		st.executeNoReplication(sql.toString());
		st.close();
		
		createIndex("permission", "base_uuid", con);
		createIndex("permission", "ctx_uuid", con);
		createIndex("permission", "created_on", con);
		
		System.out.println("Permission Table Created");
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE type (");
		sql.append("class varchar(2000) NOT NULL,");
		sql.append("type varchar(5) NOT NULL,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (class))");
		//System.out.println(sql.toString());
		st = con.createStatement();
		st.executeNoReplication(sql.toString());
		st.close();
		
		createIndex("type", "type", con);
		
		System.out.println("type Table Created");
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE repl_pull (");
		sql.append("nano bigint NOT NULL,");
		sql.append("init_on bigint NOT NULL ,");
		sql.append("comp_on bigint,");
		sql.append("server_nano bigint,");
		sql.append("sql_count int,");
		sql.append("PRIMARY KEY (nano))");
		//System.out.println(sql.toString());
		st = con.createStatement();
		st.executeNoReplication(sql.toString());
		st.close();
		
		createIndex("repl_pull", "init_on", con);
		createIndex("repl_pull", "server_nano", con);
	
		System.out.println("repl_pull Table Created");
		
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE repl_push (");
		sql.append("nano bigint NOT NULL,");
		sql.append("init_on bigint NOT NULL,");
		sql.append("comp_on bigint,");
		sql.append("server_nano bigint,");
		sql.append("sql_count int,");
		sql.append("PRIMARY KEY (nano))");
		//System.out.println(sql.toString());
		st = con.createStatement();
		st.executeNoReplication(sql.toString());
		st.close();
		
		createIndex("repl_push", "init_on", con);
		createIndex("repl_push", "server_nano", con);
	
		System.out.println("repl_push Table Created");
		
		
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE content (");
		sql.append("version_uuid varchar(42) NOT NULL,");
		sql.append("base_uuid varchar(42) NOT NULL,");
		sql.append("name varchar(256) NOT NULL,");
		sql.append("content_type varchar(100) NOT NULL,");
		sql.append("content_size bigint NOT NULL,");
		sql.append("data blob(20M) ,");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("PRIMARY KEY (version_uuid))");
		
		//System.out.println(sql.toString());
		st = con.createStatement();
		st.executeNoReplication(sql.toString());
		st.close();
		
		createIndex("content", "base_uuid", con);
		createIndex("content", "name", con);
		
		System.out.println("binary Table Created");
		
	}
	
	
	private void createIndex(String tableName, String cols, Connection con)
	throws Exception
	{
		
		Statement st = con.createStatement();
		String indexName = UUIDGenerator.randomString(6);
	
		String sql = "CREATE INDEX IDX_"+tableName+"_"+indexName.toUpperCase()+" on "+tableName+"("+cols+")";
		//System.out.println(sql);
		st.executeNoReplication(sql);
	
		
	}
	

	
	public <T extends Base> T get(String baseUuid, Class<T> t)
	{
		Base obj = null;
		try
		{
			obj = t.newInstance();
			selectBase(baseUuid, obj);
			if(obj.getBaseUuid() == null)
			{
				return null;
			}
			Map<String, Base> map = new HashMap<String, Base>();
			map.put(obj.getBaseUuid(), obj);
			
			addtMetadata(map);
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return t.cast(obj);
	}
	
	public List<Base> get(String[] baseUuids)
	{
		String uuids = StringUtil.toInClause(baseUuids);
		return get(uuids);
	}
	
	public List<Base> get(Set<String> baseUuids)
	{
		String uuids = StringUtil.toInClause(baseUuids);
		return get(uuids);
	}
	
	public List<Base> get(List<String> baseUuids)
	{
		String uuids = StringUtil.toInClause(baseUuids);
		return get(uuids);
	}
		
	private List<Base> get(String inClasueUuids)
	{
		List<Base> objList = new ArrayList<>(200);
		
		Connection con = getConnection();
		try
		{
			
			
			String query = ("select * from base where uuid in ("+inClasueUuids+")");
			Statement st = con.createStatement();
			Map<String, Base> map = new HashMap<String, Base>();
			ResultSet rs = st.executeQuery(query);
			while(rs.next())
			{
				String type = rs.getString("type");
				String objClass = getObjClass(type);
				Base obj = (Base) Class.forName(objClass).newInstance();
				buildBO(rs, obj);
				objList.add(obj);
				map.put(obj.getBaseUuid(), obj);
			}
			rs.close();
			st.close();
			
			addtMetadata(map);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
		return objList;
	}
	
	public Map<String, Base> findMap(String[] baseUuids)
	{
		Map<String, Base> objMap = new HashMap<>(200);
		
		Connection con = getConnection();
		try
		{
			
			
			String uuids = StringUtil.toInClause(baseUuids);
			String query = ("select * from base where uuid in ("+uuids+")");
			Statement st = con.createStatement();
			Map<String, Base> map = new HashMap<String, Base>();
			ResultSet rs = st.executeQuery(query);
			while(rs.next())
			{
				String type = rs.getString("type");
				String objClass = getObjClass(type);
				Base obj = (Base) Class.forName(objClass).newInstance();
				buildBO(rs, obj);
				objMap.put(obj.getBaseUuid(), obj);
				map.put(obj.getBaseUuid(), obj);
			}
			rs.close();
			st.close();
			
			addtMetadata(map);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			con.close();
		}
		
		return objMap;
	}
	
	
	public <T extends Base> T getByName(String name, Class<T> _class)
	{
		//String type = getType(_class);
		List<T> objList = findByName(name, _class);
		if(objList == null || objList.isEmpty())
		{
			return null;
		}
		
		return objList.get(0);
		
	}
	
	public <T extends Base> T getByProperty(Class<T> _class, String name, String value)
	{
		//String type = getType(_class);
		String type = getType(_class);
		List<String> list = findByProperty(type, name, value);
		if(list == null || list.isEmpty())
		{
			return null;
		}
		
		List<Base> objList = get(list.toArray(new String[list.size()]));
		if(StringUtil.isNullOrEmpty(objList))
		{
			return null;
		}
		
		return _class.cast(objList.get(0));
		
	}
	
	public <T extends Base> List<T> findByProperty(Class<T> _class, String name, String value)
	{
		//String type = getType(_class);
		String type = getType(_class);
		List<String> list = findByProperty(type, name, value);
		if(list == null || list.isEmpty())
		{
			return null;
		}
		
		List<Base> objList = get(list.toArray(new String[list.size()]));
		if(StringUtil.isNullOrEmpty(objList))
		{
			return null;
		}
		
		return (List<T>) objList;
		
	}
	
	public List<String> findByProperty(String type, String name, String value)
	{
		List<String> list = new ArrayList<>(200);
		
		Connection con = getConnection();
		try
		{
			PreparedStatement st = con.prepareStatement("select uuid from base "
					+ "where latest_version_uuid in (select uuid from version where name=? and value=?) "
					+ "and type = ?");
			st.setString(1, name);
			st.setString(2, value);
			st.setString(3, type);
			
			ResultSet rs = st.executeQuery();
			while(rs.next())
			{
				list.add(rs.getString(1));
			}
			rs.close();
			st.close();
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
		return list;
	}
		
	
	public <T extends Base> List<T> findByName(String name, Class<T> _class)
	{
		String type = getType(_class);
		List<T> objList = new ArrayList<>(200);
		
		Connection con = getConnection();
		
		try
		{
			
			PreparedStatement st = con.prepareStatement("select * from base where name like ? and type=?");
			st.setString(1, name);
			st.setString(2, type);
			
			Map<String, Base> map = new HashMap<String, Base>();
			
			ResultSet rs = st.executeQuery();
			while(rs.next())
			{
				String objClass = getObjClass(type);
				Base obj = (Base) Class.forName(objClass).newInstance();
				buildBO(rs, obj);
				objList.add((T) obj);
				map.put(obj.getBaseUuid(), obj);
			}
			rs.close();
			st.close();
			
			addtMetadata(map);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			con.close();
		}
		
		return objList;
	}
	
	public <T extends Base> List<T> find(Class<T> _class)
	{
		
		String type = getType(_class);
		List<T> objList = new ArrayList<>(200);
		Connection con = getConnection();
		try
		{
			
			PreparedStatement st = con.prepareStatement("select * from base where type=?");
			st.setString(1, type);
			Map<String, Base> map = new HashMap<String, Base>();
			ResultSet rs = st.executeQuery();
			while(rs.next())
			{
				String objClass = getObjClass(type);
				Base obj = (Base) Class.forName(objClass).newInstance();
				buildBO(rs, obj);
				objList.add((T) obj);
				map.put(obj.getBaseUuid(), obj);
			}
			rs.close();
			st.close();
			addtMetadata(map);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			con.close();
		}
		
		return objList;
	}
	

	public <T extends Base> Linked<T> getChildren(Base obj)
	{
		return getChildren(null, obj.getBaseUuid());
	}
	
	public <T extends Base> Linked<T> getChildren(Class<T> c, Base obj)
	{
		return getChildren(c, obj.getBaseUuid());
	}
	
	public <T extends Base> Linked<T> getChildren(Class<T> c, String pUuid)
	{
		
		Linked<T> linked = new Linked<>(c, pUuid, Direction.CHILD, this);
		List<String> uuids = findLinkedUuid(pUuid,c,Direction.CHILD);
		if(StringUtil.isNullOrEmpty(uuids))
		{
			return linked;
		}
		
		linked.setCount(uuids.size());
		linked.setUuids(uuids);
		
		return linked;
	}
	
	public <T extends Base> Linked<T> getParent(Class<T> c, Base obj)
	{
		return getChildren(c, obj.getBaseUuid());
	}
	
	public <T extends Base> Linked<T> getParent(Class<T> c, String cUuid)
	{
		
		Linked<T> linked = new Linked<>(c, cUuid, Direction.PARENT, this);
		List<String> uuids = findLinkedUuid(cUuid,c,Direction.PARENT);
		if(StringUtil.isNullOrEmpty(uuids))
		{
			return linked;
		}
		
		linked.setCount(uuids.size());
		linked.setUuids(uuids);
		
		return linked;
	}
	
	enum Direction
	{
		PARENT,
		CHILD;
	}
	
	
	private List<String> findLinkedUuid(String uuid, Class<?> cObjType, Direction direction)
	{
		Connection con = getConnection();
		List<String> uuidList = new ArrayList<>(500);
		try
		{
			PreparedStatement st = null;
			if(direction == Direction.CHILD)
			{
				if(cObjType != null)
				{
					st = con.prepareStatement("select c_uuid from link where p_uuid = ? and c_obj_type=? order by sort");
					String type = getType(cObjType);
					st.setString(2, type);
				}
				else
				{
					st = con.prepareStatement("select c_uuid from link where p_uuid = ? order by sort");
				}
			}
			else
			{
				if(cObjType != null)
				{
					st = con.prepareStatement("select p_uuid from link where c_uuid = ? and p_obj_type=? order by sort");
					String type = getType(cObjType);
					st.setString(2, type);
				}
				else
				{
					st = con.prepareStatement("select p_uuid from link where c_uuid = ? order by sort");
				}
			}
				
				
			st.setString(1, uuid);
			ResultSet rs = st.executeQuery();
			
			uuidList = new ArrayList<>(500);
			
			while(rs.next())
			{
				uuidList.add(rs.getString(1));
			}
			
			rs.close();
			st.close();
			
			return uuidList;
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
		return uuidList;
	}
	
	
	public String getLinkUuid(String pUuid, String cUuid)
	{
		Connection con = getConnection();
		String linkUuid = null;
		try
		{
			PreparedStatement st = con.prepareStatement("select uuid from link where p_uuid=? and c_uuid = ? ");
				
			st.setString(1, pUuid);
			st.setString(2,  cUuid);
			ResultSet rs = st.executeQuery();
			
			if(rs.next())
			{
				linkUuid = rs.getString(1);
			}
			
			rs.close();
			st.close();
			
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
		return linkUuid;
	}
	
	private Base selectBase(String baseUuid, Base obj)
	throws Exception
	{
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			st = con.prepareStatement("select * from base where uuid = ?");
			st.setString(1, baseUuid);
			rs = st.executeQuery();
			if(rs.next())
			{
				buildBO(rs, obj);
			}
			else
			{
				obj.setBaseUuid(null);
			}
			rs.close();
			st.close();
			
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
		
		return null;
		
	}
	
	private void addtMetadata(Map<String, Base> objMap)
	throws Exception
	{
		if(objMap == null || objMap.isEmpty())
		{
			return;
		}
		
		Connection con = getConnection();
		
		try
		{
			Statement st = con.createStatement();
			String inClause = StringUtil.toInClauseForMetadata(objMap.values());
			
			ResultSet rs = st.executeQuery("select * from metadata where version_uuid in ("+inClause+")");
			
			while(rs.next())
			{
				//String versionUuid = rs.getString("version_uuid");
				String baseUuid = rs.getString("base_uuid");
				String name = rs.getString("name");
				String value = rs.getString("value");
				
				Base obj = objMap.get(baseUuid);
				
				MDFieldMap fieldMap = classFieldMap.get(obj.getClass().getName());
				if(fieldMap == null)
				{
					setupMetadataFields(obj);
					fieldMap = classFieldMap.get(obj.getClass().getName());
				}
				
				Methods methods = fieldMap.get(name);
				Method setMethod = methods.set;
				
				
				Class<?> fieldType = methods.field.getType();
				
				
				if(fieldType == int.class)
				{
					int v = Integer.parseInt(value);
					setMethod.invoke(obj, new Object[]{v});
				}
				else if(fieldType == long.class)
				{
					long v = Long.parseLong(value);
					setMethod.invoke(obj, new Object[]{v});
				}
				else if(fieldType == boolean.class)
				{
					boolean v = false;
					if(value != null && value.equals("true"))
					{
						v = true;
					}
					
					setMethod.invoke(obj, new Object[]{v});
				}
				else if(fieldType == String.class || fieldType == StringBuffer.class)
				{
					setMethod.invoke(obj, new Object[]{value});
				}
				else
				{
					Object paramObj = mapper.readValue(value.getBytes(),fieldType );
					setMethod.invoke(obj, new Object[]{paramObj});
				}
				
				
				
			}
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			con.close();
		}
	
		
	}
	
	private void buildBO(ResultSet rs, Base obj) throws Exception
	{
		
		obj.setBaseUuid(rs.getString("uuid"));
		obj.setName(rs.getString("name"));
		obj.setState(rs.getString("state"));
		obj.setType(rs.getString("type"));
		obj.setContainerUuid(rs.getString("container_uuid"));
		obj.setLatestVersionUuid(rs.getString("latest_version_uuid"));
		obj.setVersionNumber(rs.getInt("latest_version_number"));
		obj.setPermissionType(rs.getString("permission_type"));
		obj.setCreatedBy(rs.getString("created_by"));
		obj.setCreatedOn(rs.getTimestamp("created_on"));
		obj.setVersionUuid(rs.getString("latest_version_uuid"));
		
		obj.setVersionUuid(obj.getLatestVersionUuid());
	}
	
	public void save(Base obj)
	{
		
		if(obj.getClass() == Base.class)
		{
			throw new LocalDBException("Base class needs to be extended and can't be used as concrete class");
		}
		
		if(StringUtil.isNullOrEmpty(obj.getBaseUuid()))
		{
			String type = getType(obj.getClass());
			obj.setType(type);
			String uuid = UUIDGenerator.uuid(type);
			String versionUuid = UUIDGenerator.uuid(type);
			
			obj.setBaseUuid(uuid);
			obj.setVersionNumber(1);
			obj.setLatestVersionUuid(versionUuid);
			obj.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			
			if(StringUtil.isNullOrEmpty(obj.getName()))
			{
				obj.setName(obj.getClass().getSimpleName()+"-"+obj.hashCode());
			}
			
			insertBase(obj);
			insertVersion(obj);
			insertMetadata(obj);
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
			insertMetadata(obj);
		}
		
		if(obj instanceof Content)
		{
			insertContent((Content)obj);
		}
	}
	
	private void insertContent(Content ct)
	{
			
		Connection con = getConnection();
		try
		{
			
			PreparedStatement st = con.prepareStatement("insert into content(version_uuid, base_uuid, name, content_type, content_size, created_by, created_on, data) "
					+ "values(?,?,?,?,?,?,?,?)");
			
			st.setString(1, ct.getLatestVersionUuid());
			st.setString(2, ct.getBaseUuid());
			st.setString(3, ct.getName());
			st.setString(4, ct.getContentType());
			st.setLong(5, ct.getSize());
			st.setString(6, ct.getCreatedBy());
			st.setTimestamp(7, ct.getCreatedOn());
			st.setBytes(8, ct.getData().get());
			
			st.executeAndReplicate(ct.getType(),"ctUuid:"+ct.getServerUuid(), "vrUuid:"+ct.getLatestVersionUuid());
				
			st.close();
			
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
	}
	
	public byte[] getContent(Base obj)
	{
		return getContent(obj.getLatestVersionUuid());
	}
	
	public byte[] getContent(String versionUuid)
	{
			
		Connection con = getConnection();
		byte[] bytes = null;
		try
		{
			
			
			PreparedStatement st = con.prepareStatement("select data from content where version_uuid=?");
			st.setString(1, versionUuid);
		
			ResultSet rs = st.executeQuery();
			if(rs.next())
			{
				bytes = rs.getBytes(1);
			}
			
					
			st.close();
			
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
		return bytes;
		
	}
	
	
	private void insertBase(Base obj)
	{
		PreparedStatement st = null;
		
		Connection con = getConnection();
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
			
			st.executeAndReplicate(obj.getType());
				
			st.close();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
	}
	
	private void updateBase(Base obj)
	{
		PreparedStatement st = null;
		
		Connection con = getConnection();
		try
		{
			st = con.prepareStatement("update base set name=?, latest_version_uuid =?, "
							+ "latest_version_number=? where uuid=? ");
				
			
			st.setString(1, obj.getName());
			st.setString(2, obj.getLatestVersionUuid());
			st.setInt(3, obj.getVersionNumber());
			st.setString(4, obj.getBaseUuid());
			
			
			st.executeAndReplicate(obj.getType());
			st.close();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
	}
	
	private void insertVersion(Base obj)
	{
		
		
		PreparedStatement st = null;
		
		Connection con = getConnection();
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
			
			st.executeAndReplicate(obj.getType());
			
			st.close();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
	}
	
	
	private void setupMetadataFields(Base obj) throws Exception
	{
		
		if(obj == null)
		{
			return;
		}
		
		if(obj.getClass() == Base.class)
		{
			return;
		}
		
		List<Class<?>> classes = new ArrayList<>();
		Class<?> clazz = obj.getClass();
		classes.add(obj.getClass());
		
		for (int i = 0; i < 10; i++)
		{
			clazz = clazz.getSuperclass();
			if(clazz == Base.class)
			{
				break;
			}
			else
			{
				classes.add(clazz);
			}
			
		}
	
		for (int i = classes.size()-1; i >= 0; i--)
		{
			//System.out.println(" ================= > "+classes.get(i));
			Class<?> _class = classes.get(i);
		
			Field[] fields = _class.getDeclaredFields();
			for (Field field : fields)
			{
				Annotation storeAnno = field.getAnnotation(Store.class);
				if(storeAnno != null)
				{
					Class<?> fieldType = field.getType();
					//System.out.println(" ---- > "+field.getName());
					String fieldName = field.getName();
					
					MDFieldMap mdFieldMap = classFieldMap.get(obj.getClass().getName());
					if(mdFieldMap == null)
					{
						mdFieldMap = new MDFieldMap();
						classFieldMap.put(obj.getClass().getName(), mdFieldMap);
					}
					
					Method getMethod = null;
					if(fieldType == boolean.class)
					{
						String getMethodName = "is"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
						getMethod = _class.getMethod(getMethodName);
						
					}
					else
					{
						String getMethodName = "get"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
						getMethod = _class.getMethod(getMethodName);
						
					}
					
					String setMethodName = "set"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
					Method setMethod = _class.getMethod(setMethodName, fieldType);
					
					Methods methods = new Methods();
					methods.get = getMethod;
					methods.set = setMethod;
					methods.field = field;
					
					mdFieldMap.put(field.getName(), methods);
					
				}
			}
		}
	}
	
	
	private void insertMetadata(Base obj)
	{
		Map<String, String> mdMap = new HashMap<>();
		
		try
		{
			
			
			
			Map<String, Methods> fieldMap = classFieldMap.get(obj.getClass().getName());
			if(fieldMap == null)
			{
				setupMetadataFields(obj);
				fieldMap = classFieldMap.get(obj.getClass().getName());
			}
		
			if(fieldMap == null)
			{
				//System.out.println("no metadata to save");
				return;
			}
			
		
			Set<String> fieldNames = fieldMap.keySet();
			
			for (String fieldName : fieldNames)
			{
					
				//System.out.println(" ---- > "+fieldName);
				
				
				//String getMethodName = "get"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
				Methods methods = fieldMap.get(fieldName);
				Method method = methods.get;
				Class<?> fieldType = methods.field.getType();
				
				Object mdObj = method.invoke(obj, null);
				String value = null;
				if(mdObj == null)
				{
					continue;
				}
				else if(fieldType.isPrimitive())
				{
					value = mdObj.toString();
				}
				else if(fieldType == String.class || fieldType == StringBuffer.class || fieldType == StringBuilder.class)
				{
					value = mdObj.toString();
				}
				else
				{
					value = mapper.writeValueAsString(mdObj);
				}
				
				mdMap.put(fieldName, value);
				
				
				
				
				
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
			
			
		if(mdMap == null || mdMap.isEmpty())
		{
			return;
		}
			
		Connection con = getConnection();
		try
		{
			
			
			PreparedStatement st = con.prepareStatement("insert into metadata(base_uuid, version_uuid, name, value,created_by, created_on) "
					+ "values(?,?,?,?,?,?)");
			
			
			for(String mdName : mdMap.keySet())
			{
				
				String value = mdMap.get(mdName);
				
				
				st.setString(1, obj.getBaseUuid());
				st.setString(2, obj.getLatestVersionUuid());
				st.setString(3, mdName);
				st.setString(4, value);
				st.setString(5, obj.getCreatedBy());
				st.setTimestamp(6, obj.getCreatedOn());
				
				st.addbatch(obj.getType());
				
			}
			
			st.executeBatch();
			st.close();
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
		
			con.close();
			
		}
		
	}
	
	
	
	
	private String getType(Class<?> _class)
	{
		
		String className = _class.getName();
		String type = classTypeMap.get(className);
		if(type != null)
		{
			return type;
		}
		
		
		type = getTypeByClassFromDB(className);
		if(type != null)
		{
			classTypeMap.put(className, type);
			typeClassMap.put(type, className);
			
			return type;
		}
		
		String hash = CryptoUtil.md5(className.getBytes());
		
		System.out.println(" hash ====> "+hash);
		
		type = hash.substring(10, 14).toUpperCase();
		
	
		Connection con = getConnection();
		try
		{
		
			PreparedStatement st = con.prepareStatement("insert into type(class,type,created_by, created_on) values (?,?,?,?)");
			st.setString(1, className);
			st.setString(2, type);
			st.setString(3, BODefaults.BASE_USER);
			st.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			st.executeAndReplicate(type);
			
			st.close();
			
			
		
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
		
		classTypeMap.put(className, type);
		typeClassMap.put(type, className);
		
		return type;
		
	
	}
	
	
	public String getObjClassByUuid(String uuid)
	{
		String type = ObjectUtil.getType(uuid);
		return getObjClass(type);
	}
	
	public String getObjClassType(String type)
	{
		
		return getObjClass(type);
	}
		
	private String getObjClass(String type)
	{
		String objClass = typeClassMap.get(type);
		if(objClass != null)
		{
			return objClass;
		}
		
		
		PreparedStatement st = null;
		ResultSet rs = null;
		
		Connection con = getConnection();
		try
		{
			st = con.prepareStatement("select * from type where type = ?");
			st.setString(1, type);
			rs = st.executeQuery();
			if(rs.next())
			{
				objClass = rs.getString("class");
			}
			
			if(objClass != null)
			{
				classTypeMap.put(objClass, type);
				typeClassMap.put(type, objClass);
				return objClass;
			}
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
		return objClass;
	}

	
	private String getTypeByClassFromDB(String className)
	{
		
		String type = null;
		Connection con = getConnection();
		try
		{
			PreparedStatement st = con.prepareStatement("select * from type where class = ?");
			st.setString(1, className);
			ResultSet rs = st.executeQuery();
			if(rs.next())
			{
				type = rs.getString("type");
			}
			
			rs.close();
			st.close();
			
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
		return type;
	}

	

	public String link(Base pObj, Base cOobj, Base... objs)
	{
		try
		{
			return insertLink(pObj.getBaseUuid(), cOobj.getBaseUuid(),10, objs);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String link(Base pObj, Base cOobj)
	{
		try
		{
			return insertLink(pObj.getBaseUuid(), cOobj.getBaseUuid(),10);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String link(String pUuid, String cUuid)
	{
		try
		{
			return insertLink(pUuid, cUuid,10);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String link(String pUuid, String cUuid, Base... objs)
	{
		try
		{
			return insertLink(pUuid, cUuid,10, objs);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	

		
	private String insertLink(String p_uuid, String c_uuid, int sort, Base... objs) throws Exception
	{
		/*
		PreparedStatement st = con.prepareStatement("insert into link"
				+ "(link_uuid,link_type,obj_type,obj_uuid,sort,seq,count, created_by, created_on) "
				+ " values(?,?,?,?,?,?,?,?,?) ");
		*/
		
		Connection con = getConnection();
		
		PreparedStatement st = con.prepareStatement("insert into link"
				+ "(uuid, p_uuid, c_uuid, p_obj_type,c_obj_type,sort, created_by, created_on) "
				+ " values(?,?,?,?,?,?,?,?) ");
		String linkUuid = UUIDGenerator.uuid("LINK");
		String pObjType = ObjectUtil.getType(p_uuid);
		String cObjType = ObjectUtil.getType(c_uuid);
		
		st.setString(1, linkUuid);
		st.setString(2, p_uuid);
		st.setString(3, c_uuid);
		st.setString(4, pObjType);
		st.setString(5, cObjType);
		st.setInt(6, sort);
		st.setString(7, BODefaults.BASE_USER);
		st.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
		
		boolean a = st.executeAndReplicate(pObjType, cObjType);
		st.close();
		
		if(objs != null && objs.length > 0)
		{
			st = con.prepareStatement("insert into link_att"
					+ "(uuid, link_uuid, name, value) "
					+ " values(?,?,?,?) ");
			
			String linkAttUuid = UUIDGenerator.uuid("LNAT");
			
			for (Base base : objs)
			{
				
				st.setString(1, linkAttUuid);
				st.setString(2, linkUuid);
				st.setString(3, "obj");
				st.setString(4, base.getBaseUuid());
				st.addbatch(null);
				
			}
			
			st.executeBatch();
		
			st.close();
		}
		
		con.close();
		return linkUuid;
	}
	
	Map<String, Set<String>> getLinkAtt(String pObjUuid, List<String> lUuids, Direction direction)
	{
		String inClause = StringUtil.toInClause(lUuids);
		return getLinkAtt(pObjUuid, inClause, direction);
	}
	
	
	
	
	private Map<String, Set<String>> getLinkAtt(String srcObjUuid, String lUuids, Direction direction)
	{
		Map<String, Set<String>> map = new HashMap<>(100);
		Connection con = getConnection();
		
		String query = null;
		
		if(direction == Direction.PARENT)
		{
			query = "select l.c_uuid, a.value from link l, link_att a where a.link_uuid=l.uuid "
					+ " and a.name='obj' and l.c_uuid=? and l.p_uuid in ("+lUuids+")";
		}
		else
		{
			query =  "select l.c_uuid, a.value from link l, link_att a where a.link_uuid=l.uuid "
					+ " and a.name='obj' and l.p_uuid=?  and l.c_uuid in ("+lUuids+")";
		}
		
		try
		{
			PreparedStatement st = con.prepareStatement(query);
			st.setString(1, srcObjUuid);
			ResultSet rs = st.executeQuery();
			while(rs.next())
			{
				String cUuid = rs.getString("c_uuid");
				String value = rs.getString("value");
				
				Set<String> set = map.get(cUuid);
				if(set == null)
				{
					set = new HashSet<>();
					map.put(cUuid, set);
				}
				set.add(value);
				
				
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
		return map;
		
	}
	
	public boolean deletLink(String pUuid, String cUuid) throws Exception
	{
		/*
		PreparedStatement st = con.prepareStatement("insert into link"
				+ "(link_uuid,link_type,obj_type,obj_uuid,sort,seq,count, created_by, created_on) "
				+ " values(?,?,?,?,?,?,?,?,?) ");
		*/
		
		String pObjType = ObjectUtil.getType(pUuid);
		String cObjType = ObjectUtil.getType(cUuid);
		
		Connection con = getConnection();
		
		PreparedStatement st = con.prepareStatement("delete from link_att where link_uuid in (select uuid from link where p_uuid = ? and c_uuid=?) ?");
		st.setString(1, pUuid);
		boolean a = st.executeAndReplicate(pObjType, cObjType);
		st.close();
		
		 st = con.prepareStatement("delete from link where p_uuid = ? and c_uuid=?");
		st.setString(1, pUuid);
		st.setString(2, cUuid);
		
		a = st.executeAndReplicate(pObjType, cObjType);
		st.close();
		
		con.close();
		return a;
	}
	
	
	Repl getRepPull(long nano) throws Exception
	{
		Connection con = getConnection();
		PreparedStatement st = con.prepareStatement("select * from repl_pull where nano = ?");
		st.setLong(1, nano);
		
		Repl repl = null;
		ResultSet rs =  st.executeQuery();
		if(rs.next())
		{
			repl = new Repl();
			repl.nano = nano;
			repl.initOn = rs.getLong("init_on");
			repl.compOn = rs.getLong("comp_on");
			repl.serverNano = rs.getLong("server_nano");
			repl.sqlCount = rs.getInt("sql_count");
		
			
		}
		rs.close();
		st.close();
		
		con.close();
		return repl;
	}
	
	
	long startRepPull() throws Exception
	{
		
		Connection con = getConnection();
		PreparedStatement st = con.prepareStatement("insert into repl_pull (nano, init_on, sql_count) values (?,?,?) ");
		long nano = System.nanoTime();
		st.setLong(1, nano);
		st.setLong(2, System.currentTimeMillis());
		st.setInt(3, 0);
		st.executeNoReplicate();
		st.close();
		
		con.close();
		return nano;
	}
	
	void updateRepPull(long nano, long serverNano, int sqlCount) throws Exception
	{
		
		Connection con = getConnection();
		
		PreparedStatement st = con.prepareStatement("update repl_pull set comp_on = ?, server_nano = ?, sql_count=? where nano=? ");
		
		st.setLong(1, System.currentTimeMillis());
		st.setLong(2, serverNano);
		st.setInt(3,  sqlCount);
		st.setLong(4, nano);
		st.executeNoReplicate();
		st.close();
		
		con.close();
		
	}
	
	
	long startRepPush(int sqlCount) throws Exception
	{
		
		Connection con = getConnection();
		
		PreparedStatement st = con.prepareStatement("insert into repl_push (nano, init_on, sql_count) values (?,?,?) ");
		long nano = System.nanoTime();
		st.setLong(1, nano);
		st.setLong(2, System.currentTimeMillis());
		st.setInt(3, sqlCount);
		st.executeNoReplicate();
		st.close();
		
		con.close();
		return nano;
	}
	
	void updateRepPush(long nano, long serverNano) throws Exception
	{
		Connection con = getConnection();
		
		PreparedStatement st = con.prepareStatement("update repl_push set comp_on = ?, server_nano = ? where nano=? ");
		
		st.setLong(1, System.currentTimeMillis());
		st.setLong(2, serverNano);
		st.setLong(3, nano);
		st.executeNoReplicate();
		st.close();
		
		con.close();
		
	}
	
	

	
	long getLastPush() throws Exception
	{
		
		Connection con = getConnection();
		
		PreparedStatement st = con.prepareStatement("select max(server_nano) from repl_push ");
		long nano = 0;
		ResultSet rs =  st.executeQuery();
		if(rs.next())
		{
			nano = rs.getLong(1);
		}
		rs.close();
		st.close();
		
		con.close();
		return nano;
		
	}
	
	long getLastPull() throws Exception
	{
		Connection con = getConnection();
		
		PreparedStatement st = con.prepareStatement("select max(server_nano) from repl_pull ");
		long nano = 0;
		ResultSet rs =  st.executeQuery();
		if(rs.next())
		{
			nano = rs.getLong(1);
		}
		rs.close();
		st.close();
		
		
		con.close();
		return nano;
		
	}
	
	
	public <T> CustomQuery<T> query(Class<T> c, String q)
	{
		if(q.contains("update") || q.contains("delete") || q.contains("insert"))
		{
			throw new LocalDBException("query can only be a select query");
		}
		
		CustomQuery<T> cq = new CustomQuery<>(c, q, this);
		
		return cq;
	}
	
	<T> RecordList<T> executeQuery(Class<T> c, CustomQuery<T> cq)
	{
		RecordList<T> records = new RecordList<>(c);
		
		Connection con = getConnection();
		try
		{
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(cq.getQuery());
			
			if(rs.next())
			{
				ResultSetMetaData md = rs.getMetaData();
				int cols = md.getColumnCount();
	            System.out.println("Column Count is " + cols);
	           
	            for (int i = 1; i <= cols; i++) 
	            {
	            	String colName = md.getColumnLabel(i);
	            	ColumnMetadata cmd = new ColumnMetadata(colName);
	            	cmd.setColIndex(i);
	            	
	            	records.addColumn(colName, cmd);
	            }
	            
	           processRS(cq, records, rs);
				
			}
			

			while (rs.next())
			{
				
				 processRS(cq, records, rs);
				
			}
			
			rs.close();
			st.close();
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			con.close();
		}
		
		return records;
	}
	
	
	private <T> void processRS(CustomQuery<T> cq, RecordList<T> records, ResultSet rs) throws Exception
	{
		
		
		if(cq.getClassT() == Record.class)
		{
			Record r = (Record) cq.getClassT().newInstance();
			for (String colName : records.getColumns())
			{
				Object v = rs.getObject(colName);
				r.add(colName, v);
			 	ColumnMetadata colMD = records.getColMetadata(colName);
			 	colMD.setMaxLen(v.toString().length());
			 	
			}
			records.add((T)r);
		}
		else
		{
			
			T t = cq.getClassT().newInstance();
			for (String colName : records.getColumns())
			{

				String pName = cq.getPropertyName(colName);
				if(pName == null)
				{
					continue;
				}
				Object v = rs.getObject(colName);
				BeanUtils.setProperty(t, pName, v);
			 	ColumnMetadata colMD = records.getColMetadata(colName);
			 	colMD.setMaxLen(v.toString().length());
			 	
			 				           
			}
			
			records.add(t);
		}
		
		
	}
	
	long executeReplicationSQL(ReplicationTO[] tos)
	{
		Connection con = getConnection();
		long lastServerNano = 0;
		try
		{
			System.out.print("[");
			//ReplicationTO lastTo = null;
			
			for (int i=0;i<tos.length;i++)
			{
				ReplicationTO to = tos[i];
				String[] sqls = to.cmd.split(";");
				
				Statement st = con.createStatement();
				for (int j = 0; j < sqls.length; j++)
				{
					
					st.addbatch(sqls[j]);
				}
				
				try
				{
					st.executeBatch();
				} 
				catch (DerbySQLIntegrityConstraintViolationException | BatchUpdateException e)
				{
					System.out.println(e.getMessage()+" : "+to.nano+" -> "+to.cmd);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				lastServerNano = to.nano;
				st.close();
				
				System.out.print(".");
				
				
				if(i % 100 == 0)
				{
					System.out.print("\n");
				}
				
				int ctUuidIdx = to.att.indexOf("ctUuid:");
				int vrUuidIdx = to.att.indexOf("vrUuid:");
				String ctUuid = null;
				String vrUuid = null;
				
				if(ctUuidIdx != -1 && vrUuidIdx != -1)
				{
					ctUuid = to.att.substring(ctUuidIdx+7,ctUuidIdx+43);
					vrUuid = to.att.substring(vrUuidIdx+7,vrUuidIdx+48);
					
					System.out.println("downloading ... content for uuid =>  "+vrUuid);
					byte[] bytes = Baltoro.cs.pullUploadedFileData(ctUuid);
					
					PreparedStatement cst = con.prepareStatement("update content set data=? where version_uuid=?");
					cst.setBytes(1, bytes);
					cst.setString(2, vrUuid);
					cst.executeNoReplicate();
					cst.close();
					
					System.out.println("saveed content ... for uuid =>  "+vrUuid);
				}
				
				
				
			}
			System.out.println("]");
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			con.close();
		}
		
		return lastServerNano;
	}
	
	
	private class MDFieldMap extends HashMap<String, Methods>
	{
	
	}
	
	private class Methods
	{
		Field field;
		Method get;
		Method set;	
		
	}


}

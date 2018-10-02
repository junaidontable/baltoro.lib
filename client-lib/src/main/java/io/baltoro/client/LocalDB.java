package io.baltoro.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.derby.impl.jdbc.EmbedConnection;
import org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.client.util.CryptoUtil;
import io.baltoro.client.util.StringUtil;
import io.baltoro.client.util.UUIDGenerator;
import io.baltoro.db.Connection;
import io.baltoro.db.PreparedStatement;
import io.baltoro.db.Statement;
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
	private Connection con;
	private static String dbName;
	
	static boolean initPull = false;
	
	
	Map<String, String> typeClassMap = new HashMap<>(100);
	Map<String, String> classTypeMap = new HashMap<>(100);
	
	Map<String, MDFieldMap> classFieldMap = new HashMap<>(1000);
	
	public static LocalDBBinary binary;
	
	
	static LocalDB instance()
	{
		
		if(db == null)
		{
			synchronized ("db-init".intern())
			{
				if(db == null)
				{
			
					//String dbName = "LDB-"+Baltoro.appName+"-"+Baltoro.serviceNames.toString().replaceAll(",", "-").replaceAll("/", "-");
					dbName = "LDB-"+Baltoro.appName+"-"+Baltoro.env+"-"+Baltoro.hostId;
					db = new LocalDB(dbName);
					db.startReplication();
				}
			}
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
			EmbedConnection _con = (EmbedConnection)DriverManager.getConnection(protocol + instUuid + ";create=true");
			_con.setAutoCommit(true);
			con = new Connection(_con);
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
		
		
		try
		{
	
			con.createStatement().executeQuery("select uuid from base WHERE uuid='1'");
			
			System.out.println("Found local database.... "+dbName);
		} 
		catch (SQLException e)
		{
			System.out.println("setting up NEW local database.... "+dbName);
		
			setupTables();
			
		}
		
				
	}
	
	
	void startReplication()
	{
		try
		{
			
			Repl repl = getRepPull(0);
			
			if(repl == null)
			{
				initPull = true;
				con.createStatement().execute("insert into repl_pull(nano, init_on, comp_on, server_nano, lcp_sql_count) values (0,"+System.currentTimeMillis()+",0,0,0)", false);
				repl = getRepPull(0);
			}
			
			if(repl.compOn <= 0)
			{
				initPull = true;
			}
			
			if(initPull)
			{
				int count = Baltoro.cs.pullReplicationCount(repl);
				con.createStatement().executeNoReplication("update repl_pull set sql_count="+count+", lcp_on="+System.currentTimeMillis()+" where nano="+repl.nano);
				System.out.println( "count -- > "+count );
				repl = getRepPull(0);
			}
			
			int count = 0;
			while(count < 10000)
			{
				count++;
				System.out.println( " Replication pull loop count ====>    "+count+", sqlCount="+repl.sqlCount+" , lcpSqlCount="+repl.lcpSqlCount);
				repl = pullReplication(repl);	
				
				if(repl.lcpSqlCount >= repl.sqlCount)
				{
					break;
				}
			}
			
			con.createStatement().executeNoReplication("update repl_pull set comp_on="+System.currentTimeMillis()+" where nano="+repl.nano);
			repl = getRepPull(0);
			
			initPull = false;
			
			long lServerPushNano = getLastPush();
			long lServerPullNano = getLastPull();
			
			//System.out.println(" lServerPushNano --> "+lServerPushNano+" , lServerPullNano -- > "+lServerPullNano);
			
		
			Replicator.start();
			//System.exit(1);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}

	}

	void cleanUp() throws Exception
	{
		deleteTables();
	}
	
	
	void cleanData()
	{
		try
		{
			Statement st = con.createStatement();
			
			st = con.createStatement();
			st.execute("delete from base", false);
			st.close();
			
			st = con.createStatement();
			st.execute("delete from version", false);
			st.close();
			
			st = con.createStatement();
			st.execute("delete from metadata", false);
			st.close();
			
			st = con.createStatement();
			st.execute("delete from link", false);
			st.close();
			
			
			st = con.createStatement();
			st.execute("delete from permission", false);
			st.close();
			
			
			st = con.createStatement();
			st.execute("delete from type", false);
			st.close();
			
			
			/*
			st = con.createStatement();
			st.execute("drop table binary", null);
			st.close();
			*/
			
			st = con.createStatement();
			st.execute("delete from rep_pull", false);
			st.close();
			
			st = con.createStatement();
			st.execute("delete from rep_push", false);
			st.close();
		
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	void deleteTables() throws Exception
	{
		Statement st = con.createStatement();
		
		st = con.createStatement();
		st.execute("drop table base", false);
		st.close();
		
		st = con.createStatement();
		st.execute("drop table version", false);
		st.close();
		
		st = con.createStatement();
		st.execute("drop table metadata", false);
		st.close();
		
		st = con.createStatement();
		st.execute("drop table link", false);
		st.close();
		
		
		st = con.createStatement();
		st.execute("drop table permission", false);
		st.close();
		
		
		st = con.createStatement();
		st.execute("drop table type", false);
		st.close();
		
		
		/*
		st = con.createStatement();
		st.execute("drop table binary", null);
		st.close();
		*/
		
		st = con.createStatement();
		st.execute("drop table rep_pull", false);
		st.close();
		
		st = con.createStatement();
		st.execute("drop table rep_push", false);
		st.close();
	}
	
	private void setupTables() throws Exception
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
		st.execute(sql.toString(), false);
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
		sql.append("name varchar(32672) NOT NULL,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (uuid))");
		//System.out.println(sql.toString());
		st = con.createStatement();
		st.execute(sql.toString(), false);
		st.close();
		
		createIndex("version", "name");
		createIndex("version", "base_uuid");
		createIndex("version", "created_on");
		
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
		st.execute(sql.toString(), false);
		st.close();
		
		createIndex("metadata", "name");
		createIndex("metadata", "base_uuid");
		createIndex("metadata", "version_uuid");
		
		System.out.println("Metadata Table Created");
		
			
		sql = new StringBuffer();
		sql.append("CREATE TABLE link (");
		sql.append("link_uuid varchar(42) NOT NULL,");
		sql.append("link_type varchar(12) NOT NULL DEFAULT 'default',");
		sql.append("obj_type varchar(5) NOT NULL,");
		sql.append("obj_uuid varchar(42) NOT NULL,");
		sql.append("sort smallint NOT NULL DEFAULT 50,");
		sql.append("seq smallint NOT NULL DEFAULT 5,");
		sql.append("count smallint NOT NULL DEFAULT 5,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL)");
		//sql.append("PRIMARY KEY (uuid, ctx_uuid))");
		
		st = con.createStatement();
		st.execute(sql.toString(), false);
		st.close();
		
		createIndex("link", "link_uuid");
		createIndex("link", "link_type");
		createIndex("link", "obj_type");
		createIndex("link", "obj_uuid");
		createIndex("link", "obj_uuid,obj_type");
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
		//System.out.println(sql.toString());
		st = con.createStatement();
		st.execute(sql.toString(), false);
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
		//System.out.println(sql.toString());
		st = con.createStatement();
		st.execute(sql.toString(), false);
		st.close();
		
		createIndex("type", "type");
		
		System.out.println("type Table Created");
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE repl_pull (");
		sql.append("nano bigint NOT NULL,");
		sql.append("init_on bigint NOT NULL ,");
		sql.append("lcp_on bigint,");
		sql.append("comp_on bigint,");
		sql.append("server_nano bigint,");
		sql.append("sql_count int,");
		sql.append("lcp_sql_count int,");
		sql.append("PRIMARY KEY (nano))");
		//System.out.println(sql.toString());
		st = con.createStatement();
		st.execute(sql.toString(), false);
		st.close();
		
		createIndex("repl_pull", "init_on");
		createIndex("repl_pull", "server_nano");
	
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
		st.execute(sql.toString(), false);
		st.close();
		
		createIndex("repl_push", "init_on");
		createIndex("repl_push", "server_nano");
	
		System.out.println("repl_push Table Created");
		
		
		/*
		sql = new StringBuffer();
		sql.append("CREATE TABLE binary (");
		sql.append("uuid varchar(42) NOT NULL,");
		sql.append("base_uuid varchar(42) NOT NULL,");
		sql.append("start_index interger NOT NULL,");
		sql.append("len smallint NOT NULL,");
		sql.append("data blob(100K) NOT NULL,");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (uuid))");
		
		System.out.println(sql.toString());
		st = con.createStatement();
		st.execute(sql.toString(), null);
		st.close();
		
		createIndex("base_uuid", "base_uuid");
		
		System.out.println("binary Table Created");
		**/
		
		
	}
	
	
	private void createIndex(String tableName, String cols)
	throws Exception
	{
		Statement st = con.createStatement();
		String indexName = UUIDGenerator.randomString(6);
	
		String sql = "CREATE INDEX IDX_"+tableName+"_"+indexName.toUpperCase()+" on "+tableName+"("+cols+")";
		//System.out.println(sql);
		st.execute(sql, false);
		st.close();
	}
	
	
	public <T extends Base> T get(String baseUuid, Class<T> _class)
	{
		Base obj = null;
		try
		{
			obj = _class.newInstance();
			selectBase(baseUuid, obj);
			Map<String, Base> map = new HashMap<String, Base>();
			map.put(obj.getBaseUuid(), obj);
			
			addtMetadata(map);
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return _class.cast(obj);
	}
	
	
		
	public List<Base> find(String[] baseUuids)
	{
		
		List<Base> objList = new ArrayList<>(200);
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
		
		return objList;
	}
	
	public Map<String, Base> findMap(String[] baseUuids)
	{
		
		Map<String, Base> objMap = new HashMap<>(200);
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
		
	
	public <T extends Base> List<T> findByName(String name, Class<T> _class)
	{
		String type = getType(_class);
		List<T> objList = new ArrayList<>(200);
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
		
		return objList;
	}
	
	public <T extends Base> List<T> find(Class<T> _class)
	{
		String type = getType(_class);
		List<T> objList = new ArrayList<>(200);
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
		
		return objList;
	}
	
	public <T extends Base> T getLinked(Class<T> _class, Base... objs)
	{
		List<T> list = findLinked(_class, objs);
		if(list != null && !list.isEmpty())
		{
			return list.get(0);
		}
		else
		{
			return null;
		}
	}
	
	public <T extends Base> List<T> findLinked(Class<T> _class, Base... objs)
	{
		String[] uuids = StringUtil.toUuids(objs);
		
		return findLinked(_class, true, null, uuids);
	}
	
	public <T extends Base> List<T> findLinked(Class<T> _class, String linkType, Base... objs)
	{
		String[] uuids = StringUtil.toUuids(objs);
		return findLinked(_class, true, linkType, uuids);
	}
	
	public <T extends Base> List<T> findLinkedByUuids(Class<T> _class, String... uuids)
	{
		return findLinked(_class, true, null, uuids);
	}
	
	
	public <T extends Base> List<T> findLinked(Class<T> _class, boolean directed, String linkType, String... uuids)
	{
		String type = getType(_class);
		//List<T> objList = new ArrayList<>(200);
		try
		{
			
			String baseUuids = StringUtil.toInClause(uuids);
			int count = uuids.length+1;
			String _linkType = StringUtil.isNullOrEmpty(linkType) ? "" : " and link_type='"+linkType+"' ";
			
			StringBuffer query = new StringBuffer();
			
			query.append("select distinct obj_uuid from link \n");
			query.append(" where link_uuid in ( select distinct link_uuid from link \n");
			query.append(" where obj_uuid in ("+baseUuids+") and count>=? "+_linkType+")\n"); 
			query.append(" and obj_type = ? and obj_uuid not in ("+baseUuids+") and seq>"+(directed ? "1" : "0"));
			
		
			
			PreparedStatement st = con.prepareStatement(query.toString());
			st.setInt(1, count);
			st.setString(2, type);
		
			 
			//if(debug)
			{
				//System.out.println(Replicator.getSQL(st.getStmt()));
				//System.out.println("type = "+type+" , count = "+count);
			}
			
			List<String> uuidList = new ArrayList<>(200);
			
			ResultSet rs = st.executeQuery();
			while(rs.next())
			{
				String uuid = rs.getString("obj_uuid");
				uuidList.add(uuid);
			}
			
			rs.close();
			st.close();
			
			if(!uuidList.isEmpty())
			{
				Map<String, Base> foundObjs = findMap(uuidList.toArray(new String[]{})); 
				List<T> objList = new ArrayList<T>(foundObjs.size());
				
				for(String uuid : uuidList)
				{
					Base obj = foundObjs.get(uuid);
					objList.add((T) obj);
				}
				
				return objList;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return new ArrayList<>();
	}
	
	
	
	private Base selectBase(String baseUuid, Base obj)
	throws Exception
	{
		
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
			rs.close();
			st.close();
			
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
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
		
	}
	
	public void save(Base obj)
	{
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
			
			st.execute(obj);
				
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
			
			
			st.execute(obj);
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
			
			st.execute(obj);
			
			st.close();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	private void setupMetadataFields(Base obj) throws Exception
	{
		
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
		
		
		PreparedStatement st = null;
		try
		{
			
			Map<String, String> mdMap = new HashMap<>();
			
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
				//System.out.println(mdObj);
				
			}
			
			st = con.prepareStatement("insert into metadata(base_uuid, version_uuid, name, value,created_by, created_on) "
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
				
				st.addbatch(obj);
				
			}
			
			st.executeBatch();
			st.close();
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
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
		
		String hash = CryptoUtil.hash(className);
		type = hash.substring(10, 14).toUpperCase().replaceAll("/", "0");
		
		
		String objClass = getObjClass(type);
		if(objClass == null)
		{
			try
			{
			
				PreparedStatement st = con.prepareStatement("insert into type(class,type,created_by, created_on) values (?,?,?,?)");
				st.setString(1, className);
				st.setString(2, type);
				st.setString(3, BODefaults.BASE_USER);
				st.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				st.execute(null);
				
				st.close();
				
				
			
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		classTypeMap.put(className, type);
		typeClassMap.put(type, className);
		
		return type;
		
	
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
		
		return objClass;
	}

	
	public String link(Base... objs)
	{
		try
		{
			return insertLink(null,50, objs);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String link(String type, Base... objs)
	{
		try
		{
			return insertLink(type,50, objs);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		//return null;
	}

		
	private String insertLink(String linkType, int sort, Base... objs) throws Exception
	{
		
		PreparedStatement st = con.prepareStatement("insert into link"
				+ "(link_uuid,link_type,obj_type,obj_uuid,sort,seq,count, created_by, created_on) "
				+ " values(?,?,?,?,?,?,?,?,?) ");
	
		String uuid = UUIDGenerator.uuid("LINK");
		
		int seq = 0;
		
		for (Base base : objs)
		{
			if(StringUtil.isNullOrEmpty(base.getBaseUuid()))
			{
				throw new Exception(base.getName() + "object has no UUID, make sure that the save meathod on this object has been called ");
			}
					
			seq++;
			st.setString(1, uuid);
			st.setString(2, StringUtil.isNullOrEmpty(linkType) ? "default" : linkType);
			st.setString(3, base.getType());
			st.setString(4, base.getBaseUuid());
			
			st.setInt(5, sort);
			st.setInt(6, seq);
			st.setInt(7, objs.length);
			st.setString(8, BODefaults.BASE_USER);
			st.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
			st.addbatch(base);
			
		}
		
		st.executeBatch();
	
		st.close();
		
		return uuid;
	}
	
	
	Repl getRepPull(long nano) throws Exception
	{
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
			repl.lcpSqlCount = rs.getInt("lcp_sql_count");
			
		}
		rs.close();
		st.close();
		
		return repl;
	}
	
	long startRepPull() throws Exception
	{
		
		PreparedStatement st = con.prepareStatement("insert into repl_pull (nano, init_on, sql_count) values (?,?,?) ");
		long nano = System.nanoTime();
		st.setLong(1, nano);
		st.setLong(2, System.currentTimeMillis());
		st.setInt(3, 0);
		st.execute(null);
		st.close();
		
		return nano;
	}
	
	void updateRepPull(long nano, long serverNano) throws Exception
	{
		PreparedStatement st = con.prepareStatement("update repl_push set comp_on = ?, server_nano = ? where nano=? ");
		
		st.setLong(1, System.currentTimeMillis());
		st.setLong(2, serverNano);
		st.setLong(3, nano);
		st.execute(null);
		st.close();
		
	}
	
	
	long startRepPush(int sqlCount) throws Exception
	{
		PreparedStatement st = con.prepareStatement("insert into repl_push (nano, init_on, sql_count) values (?,?,?) ");
		long nano = System.nanoTime();
		st.setLong(1, nano);
		st.setLong(2, System.currentTimeMillis());
		st.setInt(3, sqlCount);
		st.execute(null);
		st.close();
		
		return nano;
	}
	
	void updateRepPush(long nano, long serverNano) throws Exception
	{
		PreparedStatement st = con.prepareStatement("update repl_push set comp_on = ?, server_nano = ? where nano=? ");
		
		st.setLong(1, System.currentTimeMillis());
		st.setLong(2, serverNano);
		st.setLong(3, nano);
		st.execute(null);
		st.close();
		
	}
	
	

	
	long getLastPush() throws Exception
	{
		PreparedStatement st = con.prepareStatement("select max(server_nano) from repl_push ");
		long nano = 0;
		ResultSet rs =  st.executeQuery();
		if(rs.next())
		{
			nano = rs.getLong(1);
		}
		rs.close();
		st.close();
		
		return nano;
		
	}
	
	long getLastPull() throws Exception
	{
		PreparedStatement st = con.prepareStatement("select max(server_nano) from repl_pull ");
		long nano = 0;
		ResultSet rs =  st.executeQuery();
		if(rs.next())
		{
			nano = rs.getLong(1);
		}
		rs.close();
		st.close();
		
		return nano;
		
	}
	

	
	private Repl pullReplication(Repl repl) throws Exception
	{
		
		ReplicationTO[] tos = Baltoro.cs.pullReplication("0",""+repl.serverNano);
		
		System.out.println(" ===========> tos count "+tos.length);
		
		//System.out.print("[");
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
			catch (DerbySQLIntegrityConstraintViolationException e)
			{
				System.out.println("record already processed : "+to.nano);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			st.close();
			
			con.createStatement().executeNoReplication("update repl_pull "
					+ "set lcp_sql_count=(lcp_sql_count"+"+"+1+") "
					+", lcp_on="+System.currentTimeMillis() 
					+", server_nano="+to.nano
					+" where nano="+repl.nano);
			
			//lastTo = to;
			
			if(i % 10 == 0)
			{
				System.out.print(".");
			}
			
			if(i % 1000 == 0)
			{
				System.out.print("\n");
			}
			
			
		}
		//System.out.println("]");
		
		repl = getRepPull(repl.nano);
		return repl;
		
	}
	
	
	class Repl
	{
		long nano;
		long initOn;
		long compOn;
		long lcpOn;
		long serverNano;
		int sqlCount;
		int lcpSqlCount;
		
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

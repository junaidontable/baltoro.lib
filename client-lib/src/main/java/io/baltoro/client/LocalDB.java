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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.client.util.StringUtil;
import io.baltoro.client.util.UUIDGenerator;
import io.baltoro.db.Connection;
import io.baltoro.db.PreparedStatement;
import io.baltoro.db.Statement;
import io.baltoro.domain.BODefaults;
import io.baltoro.features.Store;
import io.baltoro.obj.Base;
import io.baltoro.to.ReplicationContext;
import io.baltoro.to.ReplicationTO;


public class LocalDB
{

	//private String framework = "embedded";
	private ObjectMapper mapper = new ObjectMapper();
	private static LocalDB db;
	private String protocol = "jdbc:derby:";

	private String instUuid;
	private Connection con;
	Map<String, String> typeClassMap = new HashMap<>(100);
	Map<String, String> classTypeMap = new HashMap<>(100);
	
	Map<String, MDFieldMap> classFieldMap = new HashMap<>(1000);
	
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
		
		try
		{
			EmbedConnection _con = (EmbedConnection)DriverManager.getConnection(protocol + instUuid + ";create=true");
			_con.setAutoCommit(true);
			con = new Connection(_con);
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		
		try
		{
			cleanUp();
			con.createStatement().executeQuery("select uuid from base WHERE uuid='1'");
		} 
		catch (SQLException e)
		{
			System.out.println("setting up local database.... "+e);
			Replicator.REPLICATION_ON = false;
			setupTables();
			Replicator.REPLICATION_ON = true;
		}
		
		try
		{
			sync();
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
		
		
		st = con.createStatement();
		st.execute("drop table lcp");
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
		sql.append("base_uuid varchar(42) NOT NULL,");
		sql.append("version_uuid varchar(42) NOT NULL,");
		sql.append("name varchar(256) NOT NULL,");
		sql.append("value varchar(32672) NOT NULL,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (base_uuid,version_uuid,name))");
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
		
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE lcp (");
		sql.append("uuid smallint NOT NULL,");
		sql.append("lcp_uuid varchar(42),");
		sql.append("lcp_millis bigint,");
		sql.append("init_sync_on timestamp,");
		sql.append("last_sync_on timestamp,");
		sql.append("PRIMARY KEY (uuid))");
		System.out.println(sql.toString());
		st = con.createStatement();
		st.execute(sql.toString());
		st.close();
		
		
		System.out.println(sql.toString());
		st = con.createStatement();
		st.execute("insert into lcp(uuid) values(1)");
		st.close();
		
		
		System.out.println("lcp Table Created");
		
		
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
			
			ResultSet rs = st.executeQuery(query);
			if(rs.next())
			{
				String type = rs.getString("type");
				String objClass = getObjClass(type);
				Base obj = (Base) Class.forName(objClass).newInstance();
				buildBO(rs, obj);
				objList.add(obj);
			}
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return objList;
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
		
		try
		{
			Statement st = con.createStatement();
			String inClause = StringUtil.toInClauseForMetadata(objMap.values());
			
			ResultSet rs = st.executeQuery("select * from metadata where version_uuid in ("+inClause+")");
			
			if(rs.next())
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
				else
				{
					setMethod.invoke(obj, new Object[]{value});
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
			System.out.println(" ================= > "+classes.get(i));
			Class<?> _class = classes.get(i);
		
			Field[] fields = _class.getDeclaredFields();
			for (Field field : fields)
			{
				Annotation storeAnno = field.getAnnotation(Store.class);
				if(storeAnno != null)
				{
					Class fieldType = field.getType();
					System.out.println(" ---- > "+field.getName());
					String fieldName = field.getName();
					
					MDFieldMap mdFieldMap = classFieldMap.get(obj.getClass().getName());
					if(mdFieldMap == null)
					{
						mdFieldMap = new MDFieldMap();
						classFieldMap.put(obj.getClass().getName(), mdFieldMap);
					}
					
					
					
					String getMethodName = "get"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
					Method getMethod = _class.getMethod(getMethodName);
					
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
			/*
			sql.append("CREATE TABLE metadata (");
			sql.append("base_uuid varchar(42) NOT NULL,");
			sql.append("version_uuid varchar(42) NOT NULL,");
			sql.append("name varchar(256) NOT NULL,");
			sql.append("value varchar(32672) NOT NULL,");
			sql.append("created_by varchar(42) NOT NULL, ");
			sql.append("created_on timestamp NOT NULL,");
			*/
			
			Map<String, String> mdMap = new HashMap<>();
			
			Map<String, Methods> fieldMap = classFieldMap.get(obj.getClass().getName());
			if(fieldMap == null)
			{
				setupMetadataFields(obj);
				fieldMap = classFieldMap.get(obj.getClass().getName());
			}
		
		
			
		
			Set<String> fieldNames = fieldMap.keySet();
			
			for (String fieldName : fieldNames)
			{
					
				System.out.println(" ---- > "+fieldName);
				
				
				String getMethodName = "get"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
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
				System.out.println(mdObj);
				
			}
			
			
			for(String mdName : mdMap.keySet())
			{
				
				String value = mdMap.get(mdName);
				
				st = con.prepareStatement("insert into metadata(base_uuid, version_uuid, name, value,created_by, created_on) "
						+ "values(?,?,?,?,?,?)");
				
				st.setString(1, obj.getBaseUuid());
				st.setString(2, obj.getLatestVersionUuid());
				st.setString(3, mdName);
				st.setString(4, value);
				st.setString(5, obj.getCreatedBy());
				st.setTimestamp(6, obj.getCreatedOn());
				
				st.execute();
				st.close();
			}
			
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	private String getType(Base obj)
	{
		
	
		String type = classTypeMap.get(obj.getClass().getName());
		if(type != null)
		{
			return type;
		}
		
		
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
				classTypeMap.put(obj.getClass().getName(), type);
				typeClassMap.put(type, obj.getClass().getName());
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
			
			classTypeMap.put(obj.getClass().getName(), type);
			typeClassMap.put(type, obj.getClass().getName());
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
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
				return type;
			}
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return objClass;
	}

	

	
	public String link(Base parent, Base child, Base context, int sortOrder) throws Exception
	{
		/*
		 * sql.append("uuid varchar(42) NOT NULL,");
		sql.append("p_uuid varchar(42) NOT NULL,");
		sql.append("c_uuid varchar(42) NOT NULL,");
		sql.append("ctx_uuid varchar(42) NOT NULL,");
		sql.append("sort smallint NOT NULL DEFAULT 1,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		
		 */
		PreparedStatement st = con.prepareStatement("insert into link(uuid, p_uuid, c_uuid, ctx_uuid, sort, created_by, created_on) "
				+ "value(?,?,?,?,?,?,?) ");
	
		String uuid = io.baltoro.util.UUIDGenerator.uuid("LINK");
		st.setString(1, uuid);
		st.setString(2, parent.getBaseUuid());
		st.setString(3, child.getBaseUuid());
		st.setString(4, context == null ? "" : context.getBaseUuid());
		st.setInt(5, sortOrder);
		st.setString(2, BODefaults.BASE_USER);
		st.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
		st.execute();
		st.close();
		
		return uuid;
	}
	
	private void updateLCP(LCP lcp) throws Exception
	{
		PreparedStatement st = con.prepareStatement("update lcp set lcp_uuid=?, lcp_millis=?, init_sync_on=?, last_sync_on=? where uuid=1");
		st.setString(1, lcp.lcpUuid);
		st.setLong(2, lcp.lcpMillis);
		st.setTimestamp(3, lcp.initSyncOn);
		st.setTimestamp(4, lcp.lastSyncOn);
		st.executeNoReplication();
		st.close();
		
	}
	
	private LCP getLCP() throws Exception
	{
		PreparedStatement st = con.prepareStatement("select * from lcp where uuid=1");
		ResultSet rs = st.executeQuery();
		LCP lcp = new LCP();
		if(rs.next())
		{
			String lcpUuid = rs.getString("lcp_uuid");
			long lcpMillis = rs.getLong("lcp_millis");
			Timestamp initSyncOn = rs.getTimestamp("init_sync_on");
			Timestamp lastSyncOn = rs.getTimestamp("last_sync_on");
			
			lcp.lcpUuid = lcpUuid;
			lcp.lcpMillis = lcpMillis;
			lcp.initSyncOn = initSyncOn;
			lcp.lastSyncOn = lastSyncOn;
			
		}
		rs.close();
		st.close();
		
		return lcp;
	}
	
	private void sync() throws Exception
	{
		LCP lcp = getLCP();
		boolean reset = lcp.initSyncOn == null ? true : false;
		ReplicationTO to = Baltoro.cs.getReplication(Baltoro.appUuid, Baltoro.instanceUuid, lcp.lcpUuid, lcp.lcpMillis, reset);
		int syncCount = to.totalCount;
		
		List<ReplicationContext> list = to.list;
		if(list == null)
		{
			lcp.lcpUuid = "";
			lcp.lcpMillis = System.currentTimeMillis()-2000;
			lcp.lastSyncOn = new Timestamp(System.currentTimeMillis());
			lcp.initSyncOn = new Timestamp(System.currentTimeMillis());
			
			updateLCP(lcp);
			return;
		}
		
		System.out.print("[");
		ReplicationContext lastCtx = null;
		for (ReplicationContext ctx : list)
		{
			Statement st = con.createStatement();
			//System.out.println(ctx.getCmd());
			//System.out.println(" --- ");
			st.executeNoReplication(ctx.getCmd());
			st.close();
			lastCtx = ctx;
			System.out.print("-");
		}
		System.out.println("]");
		
		lcp.lcpUuid = lastCtx.getUuid();
		lcp.lcpMillis = lastCtx.getMillis();
		lcp.lastSyncOn = new Timestamp(System.currentTimeMillis());
		
		if(reset)
		{
			lcp.initSyncOn = new Timestamp(System.currentTimeMillis());
		}
		
		updateLCP(lcp);
		
		
	}
	
	
	private class LCP
	{
		String lcpUuid;
		long lcpMillis;
		Timestamp initSyncOn;
		Timestamp lastSyncOn;
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

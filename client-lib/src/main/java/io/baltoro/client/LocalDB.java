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
import io.baltoro.features.Replicate;
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
	private boolean clean;
	private boolean replicate;
	
	
	Map<String, String> typeClassMap = new HashMap<>(100);
	Map<String, String> classTypeMap = new HashMap<>(100);
	
	Map<String, MDFieldMap> classFieldMap = new HashMap<>(1000);
	
	public static LocalDBBinary binary;
	
	
	public static LocalDB instance(boolean clean, boolean replicate)
	{
		if(db == null)
		{
			String dbUuid = Baltoro.instanceUuid;
			
			if(Baltoro.instanceUuid == null)
			{
				dbUuid = "INST-NO-UUID";
			}
			
			db = new LocalDB(dbUuid, clean, replicate);
		}
		return db;
	}
	
	private LocalDB(String instUuid, boolean clean, boolean replicate)
	{
		this.instUuid = instUuid;
		
		this.clean = clean;
		this.replicate = replicate;
		
		if(!replicate)
		{
			Replicator.REPLICATION_ON = false;
		}
		else
		{
			Replicator.REPLICATION_ON = true;
		}
		
		try
		{
			initLocalDB();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
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
			e.printStackTrace();
			System.exit(1);
		}
		
		
		try
		{
			if(clean)
			{
				cleanUp();
			}

			con.createStatement().executeQuery("select uuid from base WHERE uuid='1'");
		} 
		catch (SQLException e)
		{
			System.out.println("setting up local database.... "+e);
			Replicator.REPLICATION_ON = false;
			setupTables();
			
			if(replicate)
			{
				Replicator.REPLICATION_ON = true;
			}
		}
		
		try
		{
			if(replicate)
			{
				sync();
			}
			
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
		st.execute("drop table base", null);
		st.close();
		
		st = con.createStatement();
		st.execute("drop table version", null);
		st.close();
		
		st = con.createStatement();
		st.execute("drop table metadata", null);
		st.close();
		
		st = con.createStatement();
		st.execute("drop table link", null);
		st.close();
		
		
		st = con.createStatement();
		st.execute("drop table permission", null);
		st.close();
		
		
		st = con.createStatement();
		st.execute("drop table type", null);
		st.close();
		
		/*
		st = con.createStatement();
		st.execute("drop table binary", null);
		st.close();
		*/
		
		st = con.createStatement();
		st.execute("drop table lcp", null);
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
		st.execute(sql.toString(), null);
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
		st.execute(sql.toString(), null);
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
		st.execute(sql.toString(), null);
		st.close();
		
		createIndex("metadata", "name");
		createIndex("metadata", "base_uuid");
		createIndex("metadata", "version_uuid");
		
		System.out.println("Metadata Table Created");
		
		
		/*
		sql = new StringBuffer();
		sql.append("CREATE TABLE link (");
		sql.append("uuid varchar(42) NOT NULL,");
		sql.append("p_uuid varchar(42) NOT NULL,");
		sql.append("c_uuid varchar(42) NOT NULL,");
		sql.append("ctx1_uuid varchar(42) NOT NULL,");
		sql.append("ctx2_uuid varchar(42) NOT NULL,");
		sql.append("ctx3_uuid varchar(42) NOT NULL,");
		sql.append("sort smallint NOT NULL DEFAULT 1,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (uuid))");
		//System.out.println(sql.toString());
		st = con.createStatement();
		st.execute(sql.toString());
		st.close();
		
		createIndex("link", "p_uuid");
		createIndex("link", "c_uuid");
		createIndex("link", "ctx1_uuid");
		createIndex("link", "ctx2_uuid");
		createIndex("link", "ctx3_uuid");
		createIndex("link", "created_on");
		*/
		
		/*
		sql = new StringBuffer();
		sql.append("CREATE TABLE link (");
		sql.append("uuid varchar(42) NOT NULL,");
		sql.append("ctx_uuid varchar(42) NOT NULL,");
		sql.append("obj_type varchar(5) NOT NULL,");
		sql.append("sort smallint NOT NULL DEFAULT 5,");
		sql.append("seq smallint NOT NULL DEFAULT 5,");
		sql.append("count smallint NOT NULL DEFAULT 5,");
		sql.append("created_by varchar(42) NOT NULL, ");
		sql.append("created_on timestamp NOT NULL,");
		sql.append("PRIMARY KEY (uuid, ctx_uuid))");
		//System.out.println(sql.toString());
		 * 
		createIndex("link", "ctx_uuid");
		createIndex("link", "obj_type");
		createIndex("link", "count");
		createIndex("link", "ctx_uuid,obj_type,count");
		createIndex("link", "created_on");
		
		 * 
		 */
		
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
		st.execute(sql.toString(), null);
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
		st.execute(sql.toString(), null);
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
		st.execute(sql.toString(), null);
		st.close();
		
		createIndex("type", "type");
		
		System.out.println("type Table Created");
		
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
		
		
		sql = new StringBuffer();
		sql.append("CREATE TABLE lcp (");
		sql.append("uuid smallint NOT NULL,");
		sql.append("lcp_uuid varchar(42),");
		sql.append("lcp_millis bigint,");
		sql.append("init_sync_on timestamp,");
		sql.append("last_sync_on timestamp,");
		sql.append("PRIMARY KEY (uuid))");
		//System.out.println(sql.toString());
		st = con.createStatement();
		st.execute(sql.toString(), null);
		st.close();
		
		st = con.createStatement();
		st.execute("insert into lcp(uuid) values(1)", null);
		st.close();
		
		
		System.out.println("lcp Table Created");
		
		
	}
	
	
	private void createIndex(String tableName, String cols)
	throws Exception
	{
		Statement st = con.createStatement();
		String indexName = UUIDGenerator.randomString(6);
	
		String sql = "CREATE INDEX IDX_"+tableName+"_"+indexName.toUpperCase()+" on "+tableName+"("+cols+")";
		//System.out.println(sql);
		st.execute(sql, null);
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
	
	
	public <T extends Base> T findOne(String name, Class<T> _class)
	{
		String type = getType(_class);
		List<T> objList = find(name, _class);
		if(objList == null || objList.isEmpty())
		{
			return null;
		}
		
		return objList.get(0);
		
	}
		
	
	public <T extends Base> List<T> find(String name, Class<T> _class)
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
	
	public <T extends Base> T findFirstLinked(Class<T> _class, Base... objs)
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
		return findLinked(_class, null, uuids);
	}
	
	public <T extends Base> List<T> findLinkedByUuids(Class<T> _class, String... uuids)
	{
		return findLinked(_class, null, uuids);
	}
	
	
	public <T extends Base> List<T> findLinked(Class<T> _class, String linkType, String... uuids)
	{
		String type = getType(_class);
		//List<T> objList = new ArrayList<>(200);
		try
		{
			
			String baseUuids = StringUtil.toInClause(uuids);
			int count = uuids.length+1;
		
			StringBuffer query = new StringBuffer();
			
			query.append("select distinct obj_uuid from link \n");
			query.append(" where link_uuid in ( select distinct link_uuid from link \n");
			query.append(" where obj_uuid in ("+baseUuids+") and count="+count+")\n"); 
			query.append(" and obj_type = ? ");
			
		
			
			PreparedStatement st = con.prepareStatement(query.toString());
			st.setString(1, type);
		
			 
			//if(debug)
			{
				System.out.println(query);
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
	
	private <T extends Base> List<T> findLinked_old(boolean debug, Class<T> _class, String orderBy, Base... objs)
	{
		String type = getType(_class);
		//List<T> objList = new ArrayList<>(200);
		try
		{
			
			String baseUuids = StringUtil.toInClause(objs);
//			String query = "select l2.ctx_uuid, b.* from link l1, link l2, base b "+
//					" where l1.uuid = l2.uuid and b.uuid = l1.ctx_uuid "+
//					" and l2.ctx_uuid in ("+baseUuids+") and b.type = ? order by l1.sort";
//			
			
			
//			String query = "select l1.ctx_uuid, l1.sort from link l1, link l2 \n"+
//							" where l1.uuid = l2.uuid and l2.ctx_uuid in ("+baseUuids+") \n"+
//							" and l2.count = ? and l1.obj_type = ? order by l1.sort, l1.created_on desc";
//			
//			
			StringBuffer query = new StringBuffer();
			/*
			query.append("select ctx_uuid from link \n");
			query.append(" where ctx_uuid not in ("+baseUuids+") \n");
			query.append(" and obj_type = ? \n");
			query.append(" and uuid in ( select distinct uuid from link \n");
			query.append(" where ctx_uuid in ("+baseUuids+")\n"); 
			query.append(" and count = ? group by uuid having count(*) = ?)");
			*/
			/*
			query.append("select ctx_uuid from link \n");
			query.append(" where obj_type = ? \n");
			query.append(" and uuid in ( select distinct uuid from link \n");
			query.append(" where ctx_uuid in ("+baseUuids+")\n"); 
			query.append(" and count >= ? group by uuid having count(*) < ?)");
			*/
			
			query.append("select ctx_uuid from link \n");
			query.append(" where obj_type = ? \n");
			query.append(" and uuid in ( select distinct uuid from link \n");
			query.append(" where ctx_uuid in ("+baseUuids+"))\n"); 
			//query.append(" and count >= ? group by uuid having count(*) <= ?)");
			
			if(orderBy != null)
			{
				query.append("\r\n "+orderBy);
			}
			
			
			int count = objs.length+1;
			
			PreparedStatement st = con.prepareStatement(query.toString());
			st.setString(1, type);
			//st.setInt(2, count);
			//st.setInt(3, count);
			
			
			if(debug)
			{
				//System.out.println(query);
				//System.out.println("type = "+type+" , count = "+count);
			}
			
			List<String> uuidList = new ArrayList<>(20);
			
			ResultSet rs = st.executeQuery();
			while(rs.next())
			{
				String uuid = rs.getString("ctx_uuid");
				uuidList.add(uuid);
				/*
				String objClass = getObjClass(type);
				Base _obj = (Base) Class.forName(objClass).newInstance();
				buildBO(rs, _obj);
				objList.add((T) _obj);
				*/
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
	/*
	public String createLink(Base parent, Base child, Base ctx)
	{
		String uuid = null;
		try
		{
			uuid = insertLink(parent, child, ctx, null,null,5);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uuid;
	}
	
	public String createLink(Base parent, Base child, Base ctx1, Base ctx2 )
	{
		String uuid = null;
		try
		{
			uuid = insertLink(parent, child, ctx1, ctx2, null, 5);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uuid;
	}
	
	public String createLink(Base parent, Base child)
	{
		String uuid = null;
		try
		{
			uuid = insertLink(parent, child, null, null,null,5);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uuid;
	}
	
	public String createLink(Base parent, Base child, int sortOrder)
	{
		String uuid = null;
		try
		{
			uuid = insertLink(parent, child, null, null,null,sortOrder);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uuid;
	}
	
	public void removeLink(String uuid)
	{
		try
		{
			deleteLink(uuid);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String insertLink(Base parent, Base child, Base ctx1, Base ctx2, Base ctx3, int sortOrder)
	throws Exception
	{
		
		PreparedStatement st = null;
		try
		{
			st = con.prepareStatement("insert into link(uuid, p_uuid, c_uuid, ctx1_uuid, ctx2_uuid, ctx3_uuid, sort, created_by, created_on) "
					+ "values(?,?,?,?,?,?,?,?,?)");
			
			String uuid = UUIDGenerator.uuid("LINK");
			st.setString(1, uuid);
			st.setString(2, parent.getBaseUuid());
			st.setString(3, child.getBaseUuid());
			st.setString(4, ctx1 == null ? "" : ctx1.getBaseUuid());
			st.setString(5, ctx2 == null ? "" : ctx2.getBaseUuid());
			st.setString(6, ctx3 == null ? "" : ctx3.getBaseUuid());
			st.setInt(7, sortOrder);
			st.setString(8, parent.getCreatedBy());
			st.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
			
			//System.out.println(st.get);
			st.execute();
			st.close();
			
			return uuid;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void deleteLink(String uuid)
	throws Exception
	{
		
		PreparedStatement st = null;
		try
		{
			st = con.prepareStatement("delete from link where uuid = ?");
			st.setString(1, uuid);
			st.execute();
			st.close();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	*/
	
	
	
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
			
			Replicate repAnno = obj.getClass().getAnnotation(Replicate.class);
			if(repAnno != null)
			{
				String[] apps = repAnno.value();
				st.execute(apps);
			}
			else
			{
				st.execute(null);
			}
				
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
			
			Replicate repAnno = obj.getClass().getAnnotation(Replicate.class);
			if(repAnno != null)
			{
				String[] apps = repAnno.value();
				st.execute(apps);
			}
			else
			{
				st.execute(null);
			}
			
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
			
			Replicate repAnno = obj.getClass().getAnnotation(Replicate.class);
			if(repAnno != null)
			{
				String[] apps = repAnno.value();
				st.execute(apps);
			}
			else
			{
				st.execute(null);
			}
			
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
				
				st.addbatch();
				
			}
			
			Replicate repAnno = obj.getClass().getAnnotation(Replicate.class);
			if(repAnno != null)
			{
				String[] apps = repAnno.value();
				st.executeBatch(apps);
			}
			else
			{
				st.executeBatch(null);
			}
			
			st.close();
			
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	/*
	private String getType(Class<?> _class)
	{
		
		String className = _class.getSimpleName();
		String packageName = _class.getPackage().getName();
		
		StringBuffer str = new StringBuffer();
		
		str.append(className.substring(0,2).toUpperCase());
		str.append(packageName.substring(0,2).toUpperCase());
		
		return str.toString();
		
		int hash = _class.getAnnotation(annotationClass) .hashCode();
		
		System.out.println(" ********************** +"+hash);
		return ""+hash;
		
	}
	*/
	
	
	
	private String getType(Class _class)
	{
		
		String className = _class.getName();
		String type = classTypeMap.get(className);
		if(type != null)
		{
			return type;
		}
		
		
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			st = con.prepareStatement("select * from type where class = ?");
			st.setString(1, className);
			rs = st.executeQuery();
			if(rs.next())
			{
				type = rs.getString("type");
			}
			
			if(type != null)
			{
				classTypeMap.put(className, type);
				typeClassMap.put(type, className);
				return type;
			}
			st.close();
			
			
			//String packageName = _class.getPackage().getName();
			
			StringBuffer str = new StringBuffer();
			
			String simpleName = _class.getSimpleName();
			
			str.append(simpleName.substring(0,2).toUpperCase());
			//str.append(simpleName.substring(simpleName.length()-1).toUpperCase());
			str.append(_class.getName().length() == 1 ? "0"+_class.getName().length() : _class.getName().length());
			
			str.append(simpleName.substring(simpleName.length()-1).toUpperCase());
			//int index = packageName.indexOf('.');
			//String pkg = packageName.substring(index+1,index+3).toUpperCase();
			
			//str.append(pkg);
			
			type = str.toString();
			
			st = con.prepareStatement("insert into type(class,type,created_by, created_on) values (?,?,?,?)");
			st.setString(1, className);
			//type = UUIDGenerator.randomString(4);
			st.setString(2, type);
			st.setString(3, BODefaults.BASE_USER);
			st.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
			st.execute(null);
			
			rs.close();
			st.close();
			
			classTypeMap.put(className, type);
			typeClassMap.put(type, className);
			
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

	/*
	public String link(int sortOrder, Base... objs)
	{
		try
		{
			return insertLink(sortOrder, objs);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	*/
	/*
	public String insertLink(int sortOrder, Base... objs) throws Exception
	{
		
		PreparedStatement st = con.prepareStatement("insert into link(uuid, ctx_uuid, obj_type,sort,seq,count, created_by, created_on) "
				+ " values(?,?,?,?,?,?,?,?) ");
	
		String uuid = io.baltoro.util.UUIDGenerator.uuid("LINK");
		
		int seq = 0;
		for (Base base : objs)
		{
			seq++;
			st.setString(1, uuid);
			st.setString(2, base.getBaseUuid());
			st.setString(3, base.getType());
			st.setInt(4, sortOrder);
			st.setInt(5, seq);
			st.setInt(6, objs.length);
			st.setString(7, BODefaults.BASE_USER);
			st.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
			st.addbatch();
		}
		
		Replicate repAnno = objs[0].getClass().getAnnotation(Replicate.class);
		if(repAnno != null)
		{
			String[] apps = repAnno.value();
			st.executeBatch(apps);
		}
		else
		{
			st.executeBatch(null);
		}
		
		st.close();
		
		return uuid;
	}
	*/
	
	
	private String insertLink(String linkType, int sort, Base... objs) throws Exception
	{
		
		PreparedStatement st = con.prepareStatement("insert into link"
				+ "(link_uuid,link_type,obj_type,obj_uuid,sort,seq,count, created_by, created_on) "
				+ " values(?,?,?,?,?,?,?,?,?) ");
	
		String uuid = io.baltoro.util.UUIDGenerator.uuid("LINK");
		
		int seq = 0;
		for (Base base : objs)
		{
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
			st.addbatch();
		}
		
		Replicate repAnno = objs[0].getClass().getAnnotation(Replicate.class);
		if(repAnno != null)
		{
			String[] apps = repAnno.value();
			st.executeBatch(apps);
		}
		else
		{
			st.executeBatch(null);
		}
		
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
			String[] sqls = ctx.getCmd().split(";");
			for (int i = 0; i < sqls.length; i++)
			{
				Statement st = con.createStatement();
				//System.out.println(ctx.getCmd());
				//System.out.println(" --- ");
				st.executeNoReplication(sqls[i]);
				st.close();
			}
			
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

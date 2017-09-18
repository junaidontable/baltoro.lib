package io.baltoro.db;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.baltoro.client.util.StringUtil;
import io.baltoro.features.Column;
import io.baltoro.features.PK;
import io.baltoro.features.Table;

public class DBExecutor
{
	
	static Map<String , List<Fields>> classTableMap = new HashMap<>();

	private static List<Fields> getFields(Class<?> _class) throws Exception
	{
		String tableName = getTableName(_class);
		List<Fields> list = classTableMap.get(tableName);
		//System.out.println(" ))))))))))))))))))))))) 000000 : "+tableName);
		
		if(list == null)
		{
			
			System.out.println(" ))))))))))))))))))))))) 11111111 : "+tableName);
			String key = "table"+tableName;
			synchronized (key.intern())
			{
				
				System.out.println(" ))))))))))))))))))))))) 22222222 : "+tableName);
				
				list = classTableMap.get(tableName);
				if(list == null)
				{
					System.out.println(" ))))))))))))))))))))))) 3333333333333 : "+tableName);
					
					processObject(_class);
					list = classTableMap.get(tableName);
				}
			}
			
		}
		
		if(list == null || list.isEmpty())
		{
			throw new Exception("no columns or table ");
		}
		
		return list;
	}
	
	public static void insert(java.sql.Connection con, Object obj)
	throws Exception
	{
		String tableName = getTableName(obj.getClass());
		List<Fields> list = getFields(obj.getClass());
		
		StringBuffer q = new StringBuffer();
		q.append("insert into "+tableName+" (");
		for (Fields f : list)
		{
			q.append(f.colName+",");
		}
		q.delete(q.length()-1, q.length());
		q.append(")\n");
		
		q.append(" values (");
		for (Fields f : list)
		{
			q.append("?,");
		}
		q.delete(q.length()-1, q.length());
		q.append(")\n");
		
		//System.out.println(" **** > "+q.toString());
		
		PreparedStatement st = con.prepareStatement(q.toString());
		for (int i=0;i<list.size();i++)
		{
			Fields f = list.get(i);
			Object value = f.get.invoke(obj, null);
			//System.out.println(value+"   :   "+value.getClass());
			
			if(value instanceof Timestamp)
			{
				st.setTimestamp(i+1, (Timestamp) value);
			}
			else if(value instanceof Boolean)
			{
				boolean v = (boolean) value;
				st.setInt(i+1, v ? 1 : 0);
			}
			else if(value instanceof Integer)
			{
				st.setInt(i+1, (int)value);
			}
			else
			{
				st.setString(i+1, value == null ? "" : value.toString());
			}
			
		}
		
		boolean e = st.execute();
				
		st.close();
		
	}
	
	
	public static void update(java.sql.Connection con, Object obj)
	throws Exception
	{
		String tableName = getTableName(obj.getClass());
		List<Fields> list = getFields(obj.getClass());
		
		
		StringBuffer q = new StringBuffer();
		q.append("update "+tableName+" set ");
		for (Fields f : list)
		{
			if(!f.pk)
			{
				q.append(f.colName+"=?,");
			}
		}
		q.delete(q.length()-1, q.length());
		
		q.append(" where ");
		for (Fields f : list)
		{
			if(f.pk)
			{
				Object value = f.get.invoke(obj, null);
				q.append(f.colName+"='"+value.toString()+"' and");
			}
			
		}
		q.delete(q.length()-3, q.length());
		
		
		//System.out.println(" **** > "+q.toString());
		
		
		PreparedStatement st = con.prepareStatement(q.toString());
		int i = 0;
		for (Fields f:list)
		{
			
			if(f.pk)
			{
				continue;
			}
			i++;
			Object value = f.get.invoke(obj, null);
			//System.out.println(value);
			
			if(value instanceof Timestamp)
			{
				st.setTimestamp(i, (Timestamp) value);
			}
			else if(value instanceof Boolean)
			{
				boolean v = (boolean) value;
				st.setInt(i, v ? 1 : 0);
			}
			else
			{
				st.setString(i, value == null ? "" : value.toString());
			}
			
		}
		
		boolean e = st.execute();
				
		st.close();
		
		
	}
	
	public static <T> T selectOne(java.sql.Connection con, Class<T> _class, String query)
	throws Exception
	{
		
		List<Fields> list = getFields(_class);
		
		
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(query);
		Object obj = null;
		
		if(rs.next())
		{
			obj = _class.newInstance();
			for (Fields f : list)
			{
				Object o;
				if(f.field.getType() == Timestamp.class)
				{
					o = rs.getTimestamp(f.colName);
				}
				else if(f.field.getType() == int.class)
				{
					o = rs.getInt(f.colName);
				}
				else if(f.field.getType() == boolean.class)
				{
					int a = rs.getInt(f.colName);
					o = a == 1 ? true : false;
				}
				else
				{
					o = rs.getString(f.colName);
				}
				Method m = f.set;
				
				m.invoke(obj, o);
			}
			
		}
		
		rs.close();
		st.close();
	
		return _class.cast(obj);
		
	}
	
	
	
	public static <T> List<T> select(java.sql.Connection con, Class<T> _class, String query)
	throws Exception
	{
		
		
		System.out.println("query start ..... "+query);
		
		List<Fields> fList = getFields(_class);
		
		
		List<T> rList = new ArrayList<>(300);
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(query);
		Object obj = null;
		
		while(rs.next())
		{
			obj = _class.newInstance();
			for (Fields f : fList)
			{
				Object o;
				if(f.field.getType() == Timestamp.class)
				{
					o = rs.getTimestamp(f.colName);
				}
				else if(f.field.getType() == int.class)
				{
					o = rs.getInt(f.colName);
				}
				else if(f.field.getType() == boolean.class)
				{
					int a = rs.getInt(f.colName);
					o = a == 1 ? true : false;
				}
				else
				{
					o = rs.getString(f.colName);
				}
				Method m = f.set;
				m.invoke(obj, o);
				
				
			}
			
			rList.add(_class.cast(obj));
			
		}
		
		rs.close();
		st.close();
	
		
		System.out.println("query end ..... "+query);
		
		return rList;
		
	}

	private static String getTableName(Class<?> _class)
	throws Exception
	{
		Table tableAnno = _class.getAnnotation(Table.class);
		if(tableAnno == null)
		{
			throw new Exception("No Table annotation");
		}
		
		return tableAnno.value();
	}
	
	private static void processObject(Class<?> _class)
	throws Exception
	{
		
		Table tableAnno = _class.getAnnotation(Table.class);
		if(tableAnno == null)
		{
			throw new Exception("No Table annotation");
		}
		
		String tabelName = tableAnno.value();
		List<Fields> fieldList = new ArrayList<>();
		
		classTableMap.put(tabelName, fieldList);
		
		Field[] fields = _class.getDeclaredFields();
		for (Field field : fields)
		{
			Column colAnno = field.getAnnotation(Column.class);
			if(colAnno != null)
			{
				String colName = colAnno.value();
				Method getMethod = null;
				
				Class<?> fieldType = field.getType();
				String fieldName = field.getName();
				
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
				
				
				
				
				Fields f = new Fields();
				f.get = getMethod;
				f.set = setMethod;
				f.field = field;
				f.colName = StringUtil.isNullOrEmpty(colName) ? field.getName() : colName;
				PK pk = field.getAnnotation(PK.class);
				if(pk!= null)
				{
					f.pk = true;
				}
				
				fieldList.add(f);
				
				
			}
		}
	}
	
}

class Fields
{
	Field field;
	Method get;
	Method set;	
	String colName;
	boolean pk;
}

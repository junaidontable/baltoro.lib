package io.baltoro.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecordList<T> extends ArrayList<T>
{

	private static final long serialVersionUID = 1L;
	
	private Map<String, ColumnMetadata> colMD = new HashMap<>(50);
	List<ColumnMetadata> colList = new ArrayList<>(50);
	
	Map<String, Object> recordColMap = new HashMap<>(50000);
	
	private Class<T> t;
	
	RecordList(Class<T> t)
	{
		super(200);
		this.t = t;
	}

	RecordList(Class<T> t, int size)
	{
		super(size);
		this.t = t;
	}
	
	
	void addColumn(String name, ColumnMetadata md)
	{
		colMD.put(name, md);
		colList.add(md);
	}
	
	Set<String> getColumns()
	{
		return colMD.keySet();
	}
	
	
	public ColumnMetadata getColMetadata(String colName)
	{
		return colMD.get(colName);
	}
	
	public List<ColumnMetadata> getColMD()
	{
		return colList;
	}
	
	public Class<T> getClassT()
	{
		return t;
	}
}

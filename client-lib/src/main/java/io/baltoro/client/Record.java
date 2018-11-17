package io.baltoro.client;

import io.baltoro.client.util.UUIDGenerator;

public class Record
{
	
	RecordList<Record> rList;
	String rUuid;
	
	Record()
	{
		this.rUuid = UUIDGenerator.uuid("RECD");
	}
	
	void setRecordList(RecordList<Record> rList)
	{
		this.rList = rList;
	}
	
	void add(String colName, Object value)
	{
		rList.recordColMap.put(rUuid+"-"+colName, value);
		
	}
	
	
	public Object getValue(String colName)
	{
		return rList.recordColMap.get(rUuid+"-"+colName);
	}
}

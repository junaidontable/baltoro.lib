package io.baltoro.client;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.baltoro.to.ReplicationTO;

class PreparedStatement
{

	private java.sql.PreparedStatement stmt;
	/*
	private StringBuffer stmtBatch = new StringBuffer();
	private boolean batchPush = false;
	*/
	
	List<ReplicationTO> repList = null;
	
	PreparedStatement(java.sql.PreparedStatement stmt)
	{
		this.stmt = stmt;
	}
	
	
	boolean executeAndReplicate(String ... att) throws SQLException
	{
		boolean x = stmt.execute();
	
		Replicator.push(stmt, att);
		
		
		return x;
	}
	
	boolean executeNoReplicate() throws SQLException
	{
		boolean x = stmt.execute();
		return x;
	}
	
	/*
	int executeUpdate(boolean doPush, Base obj) throws SQLException
	{
		
		int x = stmt.executeUpdate();
		if(obj == null)
		{
			return x;
		}
		
		if(doPush)
		{
			Replicator.push(stmt, obj.getBaseUuid());
		}
		return x;
	}
	*/

	ResultSet executeQuery() throws SQLException
	{
		return stmt.executeQuery();
	}
	
	void close() throws SQLException
	{
		stmt.close();
	}
	
	
	void setString(int parameterIndex, String x) throws SQLException
	{
		stmt.setString(parameterIndex, x);
	}
	
	void setInt(int parameterIndex, int x) throws SQLException
	{
		stmt.setInt(parameterIndex, x);
	}
	
	void setLong(int parameterIndex, long x) throws SQLException
	{
		stmt.setLong(parameterIndex, x);
	}
	
	void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
	{
		stmt.setTimestamp(parameterIndex, x);
	}
	
	
	void setBytes(int parameterIndex, byte[] x) throws SQLException
	{
		stmt.setBytes(parameterIndex, x);
	}
	
	
	void addbatch(String ... att) throws SQLException
	{

		if(repList == null)
		{
			repList = new ArrayList<>();
		}
		
		ReplicationTO repTO = Replicator.create(Replicator.getSQL(stmt), att);
		repList.add(repTO);
			
		
		
		stmt.addBatch();

	}
	
	void executeBatch() throws SQLException
	{
		
		Set<String> attSet = new HashSet<>();
		StringBuffer sqls = new StringBuffer();
		StringBuffer attBuffer = new StringBuffer();
		
		for (ReplicationTO to : repList)
		{
			String[] item = to.att.split(" ");
			for (int i = 0; i < item.length; i++)
			{
				attSet.add(item[i]);
			}
			
			sqls.append(to.cmd+"\n<BLT-BLT>\n");
		}
		
		sqls.delete(sqls.length()-2, sqls.length());
		
		attSet.stream().forEach((a) -> 
		{
			attBuffer.append(a+" ");
		});
		
		Replicator.pushBatch(sqls.toString(), attBuffer.toString());
		stmt.executeBatch();
		
		repList = null;
		
	}


	java.sql.PreparedStatement getStmt()
	{
		return stmt;
	}
	
	
}

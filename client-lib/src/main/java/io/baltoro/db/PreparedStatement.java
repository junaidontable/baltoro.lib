package io.baltoro.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.baltoro.client.Replicator;
import io.baltoro.features.NoReplication;
import io.baltoro.obj.Base;
import io.baltoro.to.ReplicationTO;

public class PreparedStatement
{

	private java.sql.PreparedStatement stmt;
	/*
	private StringBuffer stmtBatch = new StringBuffer();
	private boolean batchPush = false;
	*/
	
	List<ReplicationTO> repList = null;
	
	public PreparedStatement(java.sql.PreparedStatement stmt)
	{
		this.stmt = stmt;
	}
	
	
	public boolean executeAndReplicate(String ... att) throws SQLException
	{
		boolean x = stmt.execute();
	
		Replicator.push(stmt, att);
		
		
		return x;
	}
	
	public boolean executeNoReplicate() throws SQLException
	{
		boolean x = stmt.execute();
		return x;
	}
	
	/*
	public int executeUpdate(boolean doPush, Base obj) throws SQLException
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

	public ResultSet executeQuery() throws SQLException
	{
		return stmt.executeQuery();
	}
	
	public void close() throws SQLException
	{
		stmt.close();
	}
	
	
	public void setString(int parameterIndex, String x) throws SQLException
	{
		stmt.setString(parameterIndex, x);
	}
	
	public void setInt(int parameterIndex, int x) throws SQLException
	{
		stmt.setInt(parameterIndex, x);
	}
	
	public void setLong(int parameterIndex, long x) throws SQLException
	{
		stmt.setLong(parameterIndex, x);
	}
	
	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
	{
		stmt.setTimestamp(parameterIndex, x);
	}
	
	public void addbatch(String ... att) throws SQLException
	{

		if(repList == null)
		{
			repList = new ArrayList<>();
		}
		
		ReplicationTO repTO = Replicator.create(Replicator.getSQL(stmt), att);
		repList.add(repTO);
			
		
		
		stmt.addBatch();

	}
	
	public void executeBatch() throws SQLException
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
			
			sqls.append(to.cmd+";\n");
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


	public java.sql.PreparedStatement getStmt()
	{
		return stmt;
	}
	
	
}

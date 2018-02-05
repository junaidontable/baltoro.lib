package io.baltoro.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import io.baltoro.client.Replicator;

public class PreparedStatement
{

	private java.sql.PreparedStatement stmt;
	private StringBuffer stmtBatch = new StringBuffer();
	
	public PreparedStatement(java.sql.PreparedStatement stmt)
	{
		this.stmt = stmt;
	}
	
	
	public boolean execute(String[] apps) throws SQLException
	{
		boolean x = stmt.execute();
		Replicator.push(stmt, apps);
		return x;
	}
	
	public int executeUpdateNoReplication() throws SQLException
	{
		return stmt.executeUpdate();
	}
	
	public boolean executeNoReplication() throws SQLException
	{
		return stmt.execute();
	}
	
	public int executeUpdate(String[] apps) throws SQLException
	{
		
		int x = stmt.executeUpdate();
		Replicator.push(stmt, apps);
		return x;
	}
	

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
	
	public void addbatch() throws SQLException
	{
		stmtBatch.append(Replicator.getSQL(stmt)+";");
		stmt.addBatch();
	}
	
	public void executeBatch(String[] apps) throws SQLException
	{
		Replicator.push(stmtBatch.toString(), apps);
		stmt.executeBatch();
		stmtBatch = new StringBuffer();
	}
}

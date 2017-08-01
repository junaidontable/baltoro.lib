package io.baltoro.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import io.baltoro.client.Replicator;

public class PreparedStatement
{

	private java.sql.PreparedStatement stmt;
	
	public PreparedStatement(java.sql.PreparedStatement stmt)
	{
		this.stmt = stmt;
	}
	
	
	public boolean execute() throws SQLException
	{
		Replicator.push(stmt);
		return stmt.execute();
	}
	
	public int executeUpdate() throws SQLException
	{
		Replicator.push(stmt);
		return stmt.executeUpdate();
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
}

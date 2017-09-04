package io.baltoro.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.baltoro.client.Replicator;

public class Statement
{

	private java.sql.Statement stmt;
	
	public Statement(java.sql.Statement stmt)
	{
		this.stmt = stmt;
	}
	
	public boolean execute(String sql, String[] apps) throws SQLException
	{
		Replicator.push(sql, apps);
		return stmt.execute(sql);
	}
	
	public ResultSet executeQuery(String sql) throws SQLException
	{
		return stmt.executeQuery(sql);
	}
	
	public boolean executeNoReplication(String sql) throws SQLException
	{
		return stmt.execute(sql);
	}
	
	public void addbatch(String sql) throws SQLException
	{
		stmt.addBatch(sql);
	}

	public void close() throws SQLException
	{
		stmt.close();
	}
	

	
}

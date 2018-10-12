package io.baltoro.client;

import java.sql.ResultSet;
import java.sql.SQLException;

class Statement
{

	private java.sql.Statement stmt;
	
	Statement(java.sql.Statement stmt)
	{
		this.stmt = stmt;
	}
	
	boolean executeAndReplicate(String sql) throws SQLException
	{
		Replicator.push(sql, null);
		
		return stmt.execute(sql);
	}
	
	ResultSet executeQuery(String sql) throws SQLException
	{
		return stmt.executeQuery(sql);
	}
	
	boolean executeNoReplication(String sql) throws SQLException
	{
		return stmt.execute(sql);
	}
	
	void addbatch(String sql) throws SQLException
	{
		stmt.addBatch(sql);
	}

	void close() throws SQLException
	{
		stmt.close();
	}
	
	
	int[] executeBatch() throws SQLException
	{
		return stmt.executeBatch();
	}

	
}

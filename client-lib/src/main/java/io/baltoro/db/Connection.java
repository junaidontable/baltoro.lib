package io.baltoro.db;

import java.sql.SQLException;

public class Connection
{

	private java.sql.Connection con;
	
	public Connection(java.sql.Connection con)
	{
		this.con = con;
	}
	
	
	public Statement createStatement()
	throws SQLException
	{
		return new Statement(con.createStatement());
	}
	

	public PreparedStatement prepareStatement(String sql)
	throws SQLException
	{
		return new PreparedStatement(con.prepareStatement(sql));
	}
	
	
	

	
}

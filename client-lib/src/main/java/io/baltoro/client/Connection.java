package io.baltoro.client;

import java.sql.SQLException;

class Connection
{

	private java.sql.Connection con;
	
	Connection(java.sql.Connection con)
	{
		this.con = con;
	}
	
	
	Statement createStatement()
	throws SQLException
	{
		return new Statement(con.createStatement());
	}
	

	PreparedStatement prepareStatement(String sql)
	throws SQLException
	{
		return new PreparedStatement(con.prepareStatement(sql));
	}
	
	
	void close()
	{
		LocalDB.connectionQueue.add(this);
	}

	
}

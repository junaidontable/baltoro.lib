package io.baltoro.client;

import java.io.IOException;
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
	

	public void close1() throws IOException
	{
		LocalDB.connectionQueue.add(this);	
	}
	
	
	void close()
	{
		LocalDB.connectionQueue.add(this);
	}

	
}

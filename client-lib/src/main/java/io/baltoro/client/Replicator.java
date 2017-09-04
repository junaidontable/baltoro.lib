package io.baltoro.client;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;

import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.iapi.sql.ParameterValueSet;
import org.apache.derby.iapi.types.DataValueDescriptor;
import org.apache.derby.impl.jdbc.EmbedPreparedStatement42;

import io.baltoro.client.util.ObjectUtil;
import io.baltoro.to.ReplicationContext;
import io.baltoro.to.WSTO;

public class Replicator
{

	static boolean REPLICATION_ON = false;
	static boolean INT_SYNC = false;

	
	public static String getSQL(PreparedStatement st)
	{
		
		EmbedPreparedStatement42 stmt = (EmbedPreparedStatement42) st;
		ParameterValueSet params = stmt.getParms();

		//ParameterValueSet sparams = params;
		
		int quote, dblquote, question, current, length;
		
		String thesql = stmt.getSQLText();
		StringBuffer postsql = new StringBuffer();
		
		String theparam = null;
		
		
		
		int i = 0;
		current = 0;
		length = thesql.length();
		while (current < length)
		{
			quote = thesql.indexOf("'", current);
			dblquote = thesql.indexOf("\"", current);
			question = thesql.indexOf("?", current);
			if (quote == -1)
			{
				quote = length;
			}
			if (dblquote == -1)
			{
				dblquote = length;
			}
			if (question == -1)
			{
				question = length;
			}
			if ((question < quote) && (question < dblquote))
			{
				try
				{
					DataValueDescriptor ds = params.getParameter(i++);
					String type = ds.getTypeName();
					//System.out.println(" type >>>>>>>>>>>>> "+type);
					
					if(type.contains("INT"))
					{
						theparam = ds.getString();
					}
					else
					{
						theparam = "'" + ds.getString() + "'";
					}
					
					//theparam = params.getParameter(i++).getString();
				} 
				catch (StandardException se)
				{
					/* non-string parameter */
				}
				
				postsql.append(thesql.substring(current, question));
				
				if (theparam == null)
				{
					postsql.append("null");
				} 
				else
				{
					postsql.append(theparam);
				}
				
				current = question + 1;
				
			} 
			else if (quote < dblquote)
			{
				quote = thesql.indexOf("'", quote + 1);
				postsql.append(thesql.substring(current, quote + 1));
				current = quote + 1;
			}
			else if (dblquote < length)
			{
				dblquote = thesql.indexOf("\"", dblquote + 1);
				postsql.append(thesql.substring(current, dblquote + 1));
				current = dblquote + 1;
			} 
			else
			{
				postsql.append(thesql.substring(current, length));
				current = length;
			}
		}
		return postsql.toString();
		
	}
	
	public static void push(PreparedStatement st, String[] apps)
	{
		if(!REPLICATION_ON)
		{
			return;
		}
		
		String sql = getSQL(st);
		push(sql, apps);
	}
	

	
	public static void push(String sql, String[] apps)
	{
		
		
		
		if(!REPLICATION_ON)
		{
			return;
		}
		
		//System.out.println("pushing ... "+sql);
		
		WSTO to = new WSTO();
	
		to.instanceUuid = Baltoro.instanceUuid;
		to.appUuid = Baltoro.appUuid;
		to.appName = Baltoro.appName;
		
		
		ReplicationContext ctx = new ReplicationContext();
		ctx.setMillis(System.currentTimeMillis());
		ctx.setCmd(sql);
		
		if(apps != null)
		{
			ctx.setApps(apps);
		}
		
		to.replicationContext = ctx;
		
		byte[] bytes = null;
		try
		{
			bytes = ObjectUtil.toJason(to);
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ByteBuffer  msg = ByteBuffer.wrap(bytes);
		
		WSSessions.get().addToResponseQueue(msg);
		
	}
	
	

}

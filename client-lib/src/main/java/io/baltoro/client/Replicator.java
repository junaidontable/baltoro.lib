package io.baltoro.client;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.iapi.sql.ParameterValueSet;
import org.apache.derby.iapi.types.DataValueDescriptor;
import org.apache.derby.impl.jdbc.EmbedPreparedStatement42;
import org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.client.util.StringUtil;
import io.baltoro.to.ReplicationTO;

public class Replicator
{

	//static boolean REPLICATION_ON = false;
	//static boolean INT_SYNC = false;
	static boolean runnig = false;
	static private Timer pusher;
	static private Timer puller;
	private static ConcurrentLinkedQueue<ReplicationTO> pushQueue = new ConcurrentLinkedQueue<>();
	private static ObjectMapper mapper = new ObjectMapper();
	
	private static LocalDB db = Baltoro.getDB();
	private static boolean pullDone = false;
	
	static
	{
		if(!runnig)
		{
			synchronized ("db-running".intern())
			{
				if(!runnig)
				{
					initReplicator();
					runnig = true;
				}
			}
			
		}
	}
	
	static void start()
	{
		
	}
	
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
					se.printStackTrace();
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
	
	public static void push(PreparedStatement st, String ... att)
	{

		String sql = getSQL(st);
		
		ReplicationTO obj = new ReplicationTO();
		obj.nano = System.nanoTime();
		obj.cmd = sql;
		obj.att = getAtt(sql, att);
				
		pushQueue.add(obj);
	}
	
	public static void pushBatch(String sqls, String att)
	{

		ReplicationTO obj = new ReplicationTO();
		obj.nano = System.nanoTime();
		obj.cmd = sqls;
		obj.att = att;
				
		pushQueue.add(obj);
	}
	
	public static void push(String sql, String att)
	{

		ReplicationTO obj = new ReplicationTO();
		obj.nano = System.nanoTime();
		obj.att = att;
		obj.cmd = sql;
				
		pushQueue.add(obj);
	}
	
	public static ReplicationTO create(String sql, String ... att)
	{
		
		ReplicationTO to = new ReplicationTO();
		to.nano = System.nanoTime();
		to.att = getAtt(sql, att);
		to.cmd = sql;
		
		return to;
	}
	
	
	
	private static String getAtt(String sql, String ... att)
	{
		StringBuffer token = new StringBuffer();
		
		if(sql.startsWith("insert into link_att"))
		{
			token.append("LKAT");
		}
		else if(sql.startsWith("insert into link"))
		{
			token.append("LINK");
		}
		else if(sql.startsWith("insert into base"))
		{
			token.append("BASE");
		}
		else if(sql.startsWith("insert into version"))
		{
			token.append("VERN");
		}
		else if(sql.startsWith("insert into metadata("))
		{
			token.append("MTDT");
		}
		else if(sql.startsWith("insert into permission"))
		{
			token.append("PERM");
		}
		
		
		String sNames = Baltoro.serviceNames.toString().replaceAll(",", " ").toUpperCase();
		token.append(" "+sNames+" ");
		
		if(StringUtil.isNotNullAndNotEmpty(att))
		{
			for (int i = 0; i < att.length; i++)
			{
				token.append(att[i]+" ");
			}
		}
		
		
		return token.toString();
			
	}
	
	
	private static void initReplicator()
	{
		pusher = new Timer();
		pusher.schedule(new TimerTask()
		{
			
			@Override
			public void run()
			{
				
				
				try
				{
					if(!pullDone)
					{
						System.out.println("Replicator ====>   waiting for pull to finish ....... wait 5 secs ");
						Thread.sleep(5000);
						return;
					}
					
					
					boolean hasMore = true;
			
					List<ReplicationTO> list = new ArrayList<>(100);
					while(hasMore)
					{
						ReplicationTO obj = pushQueue.poll();
						if(obj == null || list.size() > 100)
						{
							hasMore = false;
							break;
						}
						else
						{
							list.add(obj);
						}
						
					}
					
					
					if(list.size() > 0)
					{
						System.out.println("... pushing "+list.size()+" data ");
						String json = mapper.writeValueAsString(list);
						long nano = db.startRepPush(list.size());
						String _servernano = Baltoro.cs.pushReplication(json);
						long servernano = Long.parseLong(_servernano);
								
						db.updateRepPush(nano, servernano);
						
						System.out.println(" last push nano ==> "+db.getLastPush());
					}
					
					
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
					
			}
		}, 1000, 1000);
		
		
		puller = new Timer();
		puller.schedule(new TimerTask()
		{
			
			@Override
			public void run()
			{
				
				try
				{
					
					long lServerPushNano = db.getLastPush();
					long lServerPullNano = db.getLastPull();
					
					//long nano = db.startRepPull();
					
					//ReplicationTO[] tos = Baltoro.cs.pullReplication(""+lServerPushNano, ""+lServerPullNano);
					
					//System.out.println(" lServerPushNano --> "+lServerPushNano+" , lServerPullNano -- > "+lServerPullNano);
					
					pullReplication(lServerPushNano, lServerPullNano);
				
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
					
			}
		}, 1000, 1000);
	}
	
	
	private static long pullReplication(long lServerPushNano, long lServerPullNano) throws Exception
	{
		
		ReplicationTO[] tos = Baltoro.cs.pullReplication(""+lServerPushNano,""+lServerPullNano);
		if(StringUtil.isNullOrEmpty(tos))
		{
			if(!pullDone)
			{
				System.out.println("INIT PULL finished ....... ");
			}
			pullDone = true;
			
			return 0;
		}
		
		System.out.println(" ===========> pulling replicated records receiving..... "+tos.length);
		
		long nano = db.startRepPull();
		
		System.out.print("[");
		//ReplicationTO lastTo = null;
		long lastServerNano = 0;
		for (int i=0;i<tos.length;i++)
		{
			ReplicationTO to = tos[i];
			String[] sqls = to.cmd.split(";");
			Statement st = db.getConnection().createStatement();
			for (int j = 0; j < sqls.length; j++)
			{
				
				st.addbatch(sqls[j]);
			}
			
			try
			{
				st.executeBatch();
			} 
			catch (DerbySQLIntegrityConstraintViolationException e)
			{
				System.out.println("record already processed : "+to.nano);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			lastServerNano = to.nano;
			st.close();
			
			System.out.print(".");
			
			
			if(i % 100 == 0)
			{
				System.out.print("\n");
			}
			
			
		}
		System.out.println("]");
		
		
		db.updateRepPull(nano, lastServerNano, tos.length);
		
		
		return tos.length;
		
	}
	
	

}

class Repl
{
	long nano;
	long initOn;
	long compOn;
	long lcpOn;
	long serverNano;
	int sqlCount;
	int lcpSqlCount;
	
}

package io.baltoro;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.baltoro.db.DB;
import io.baltoro.db.Session;

public class RequestWorker_del extends Thread 
{
	private Baltoro b;
	boolean run = true;
	private ObjectMapper mapper = new ObjectMapper();
	
	static ThreadLocal<ReqContext> ctx = new ThreadLocal<ReqContext>();
	int count;
	

	RequestWorker_del(Baltoro b, int count) 
	{
		this.b = b;
		this.count = count;
		this.setName("RequestWorker-"+count);
	}

	
	@Override
	public void run() 
	{
		while (run) 
		{
			Request req = null;
			Session s = null;
			try 
			{
				String json = RequestQueue_del.instance().get();

				if (json == null) 
				{
					synchronized (RequestQueue_del.SYNC_KEY.intern()) 
					{
						// System.out.println("waiting for request .. .......");
						RequestQueue_del.SYNC_KEY.intern().wait(10000);
						continue;
					}
				}

				req = mapper.readValue(json, Request.class);

				
				s = DB.getSession();
				
				
				APIMethod m = APIMap.getInstance().getMethod(req.cmd);
				
				
					

				//System.out.println("====> "+api);
				//api.init(req, ctx);
				Response res = null;//api.process();
				
				
				WebClient.instance().response(res);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				Response res = new Response();
				res.reqUuid = req.reqUuid;
				res.setError("{ \"error\":\""+e.getMessage()+"\"}");
				WebClient.instance().response(res);
				//ResponseQueue.instance().add(res);

			} 
			finally 
			{
				ctx.remove();
				if(s != null)
				{
					s.freeSession();
				}
				DB.freeSession();
			}
		}
	}
	
	/*
	@Override
	public void run() 
	{
		while (run) 
		{
			Request req = null;
			Session s = null;
			try 
			{
				String json = RequestQueue.instance().get();

				if (json == null) 
				{
					synchronized (RequestQueue.SYNC_KEY.intern()) 
					{
						// System.out.println("waiting for request .. .......");
						RequestQueue.SYNC_KEY.intern().wait(10000);
						continue;
					}
				}

				req = mapper.readValue(json, Request.class);

				
				s = DB.getSession();
				
				
				API api = null;
				ReqContext ctx = checkCtx(req, s);
			
				String cName = req.cmd.replace('/', '.');
				int idx0 = cName.toLowerCase().indexOf("exec");
				if (idx0 != -1) 
				{
					int idx1 = cName.toLowerCase().indexOf(".", idx0);
					cName = req.cmd.substring(idx0, idx1);
				}

				api = (API) Class.forName("com.mckinsey.op.api." + cName).newInstance();
				

				//System.out.println("====> "+api);
				api.init(req, ctx);
				Response res = api.process();
				
				
				WebClient.instance().response(res);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				Response res = new Response();
				res.reqUuid = req.reqUuid;
				res.setError("{ \"error\":\""+e.getMessage()+"\"}");
				WebClient.instance().response(res);
				//ResponseQueue.instance().add(res);

			} 
			finally 
			{
				ctx.remove();
				if(s != null)
				{
					s.freeSession();
				}
				DB.freeSession();
			}
		}
	}

	
	private ReqContext checkCtx(Request req, Session s) throws Exception 
	{
		Log.log.info("ecn tkn =====> "+req.tkn);
		String tkn = CryptoUtil.decrypt(Outpost.ENCODED_KEY, req.tkn);
		//String tkn = new String(Base64.getDecoder().decode(_tkn));
		Log.log.info("dc tkn =====> "+tkn);
		
	    String[] tkns = tkn.split(",");
	    String sessionId = tkns[0];
	    String userId = tkns[1];
	    String appUuid = tkns[2];
		

		if (StringUtil.isNullOrEmpty(sessionId) && Outpost.config != null) 
		{
			throw new Exception("PermissionDenied as OP_SESSIONID does not exist");
		}

		ReqContext rctx = new ReqContext();
		rctx.sessionId = sessionId;
		if (StringUtil.isNotNullAndNotEmpty(userId)) 
		{
			User u = Obj.instance().getByName(userId, Obj.BASE_CONTAINER, User.class);
			if(u == null)
			{
				u = Obj.instance().makeNew(User.class);
				u.setName(userId);
				u.setContUuid(Obj.BASE_CONTAINER);
				u.setRoles("default");
				u.save();
			}
			rctx.user = u;
				
			if(u.getRoleSet() == null)
			{
				Set<String> roleSet = new HashSet<>();
				StringTokenizer _roles = new StringTokenizer(u.getRoles(), ",");
				while(_roles.hasMoreTokens())
				{
					roleSet.add(_roles.nextToken());
				}
				u.setRoleSet(roleSet);
			}
			
		}
		ctx.set(rctx);
		return rctx;
	}
	
	*/

}
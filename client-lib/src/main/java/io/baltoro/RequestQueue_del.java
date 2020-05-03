package io.baltoro;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestQueue_del
{

	Queue<String> q = new ConcurrentLinkedQueue<String>();
	
	static String SYNC_KEY = RequestQueue_del.class.getName();
	
	private static RequestQueue_del rq;
	
	private RequestQueue_del()
	{
	}
	
	static RequestQueue_del instance()
	{
		if(rq == null)
		{
			rq = new RequestQueue_del();
		}
		return rq;
	}
	
	void add(String json)
	{
		q.add(json);
		
		synchronized (SYNC_KEY.intern())
		{
			SYNC_KEY.intern().notify();
		}
	}
	
	String get()
	{
		String json = q.poll();
		//System.out.println("Request JSON = " + json);
		//Request Queue json ={"reqUuid":"c1bd2ae3-29c4-454c-8ebf-a05e0bcc2c96","clientUuid":"7035D5E77FFB47659533E0FBE3D8AE04_D1CC","cmd":"exec","url":"http://13.59.60.3/api/finlabs/sid-envdev/exec","uri":"/api/finlabs/sid-envdev/exec","relativePath":"exec","appName":"sid","clientName":"finlabs","headers":{"accept-language":"en-US,en;q=0.9","host":"13.59.60.3","upgrade-insecure-requests":"1","connection":"keep-alive","cache-control":"max-age=0","accept-encoding":"gzip, deflate","user-agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36","accept":"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3"},"cookies":{},"values":{"id":"ping2"}}
		return json;
	}
}

package io.baltoro;

import io.baltoro.db.DB;
import io.baltoro.util.Log;

public class PollWorker extends Thread
{
	private Baltoro b;
	boolean run = true;
	int errorCount = 0;
	int count;
	
	PollWorker(Baltoro b, int count)
	{
		this.b = b;
		this.count = count;
		this.setName("Poller-"+count);
	}
	
	
	@Override
	public void run()
	{
		while(run)
		{
			try
			{
				
				String json = WebClient.instance().poll();
				errorCount = 0;
				if(json.equals("none:no-response"))
				{
					System.out.println("POLL Response from SS <do nothing> ... DATABASE CONNECTION COUNT ["+DB.conCreateCount+"]");
				}
				else
				{
					//RequestQueue.instance().add(json);
					//System.out.println("Request Poller JSON ... DATABASE CONNECTION COUNT ["+DB.conCreateCount+"]");
					
					RequestWorker worker = new RequestWorker(json);
					Baltoro.workers.submit(worker);
				}

			} 
			catch (Exception e)
			{
				
				errorCount++;
				
				Log.log.info(" >>>> error reaching server .... "+errorCount);
				if(errorCount > 3)
				{
					Log.log.info("too many errors exiting ..... ");
					System.exit(1);
				}
				try
				{
					Thread.sleep(2000);
				} 
				catch (InterruptedException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				e.printStackTrace();
			}
		}
	}


	

}

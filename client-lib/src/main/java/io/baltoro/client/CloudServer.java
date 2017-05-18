package io.baltoro.client;

public class CloudServer
{
	
	/*
	static Log log = LogFactory.getLog(CloudServer.class);
	
	Client client;
	Cookie sessionCookie;
	//String host = "http://api.baltoro.org:8080";
	String host = "http://127.0.0.1:8080";
	
	Agent app;
	boolean online = false;
	
	public CloudServer(Agent app)
	{
		client = ClientBuilder.newBuilder()
				.register(JacksonFeature.class)
				.register(CheckRequestFilter.class)
				.register(CheckResponseFilter.class)
				.build();
		
		this.app = app;
	
		try
		{
			areYouThere();
			online = true;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			online = false;
		}
	}
	
	
	void areYouThere() throws Exception
	{
		log.info("... Are you There ...");
	
		WebTarget target = client.target(host).path("/baltoro/api/areyouthere");	
		Invocation.Builder ib =	getIB(target);
		Response response = ib.get();
		String sessionId = response.readEntity(String.class);
		this.sessionCookie = new Cookie("JSESSIONID", sessionId,"/", null);
		//handleSessionCookie(response);
	}
	
	///*
	void handleSessionCookie(Response response) throws Exception
	{
		Map<String, NewCookie> map = response.getCookies();
		for (String key : map.keySet())
		{
			NewCookie cookie = map.get(key);
			log.info(key+" : "+cookie);
			if(key.equals("JSESSIONID"))
			{
				String domain = cookie.getDomain();
				sessionCookie = new Cookie(cookie.getName(), cookie.getValue(),cookie.getPath(), domain);
			}
		}	
	}
	
	
	UserTO login(String email, String password) throws Exception
	{
		WebTarget target = client.target(host).path("/baltoro/api/auth/login");	
	
		Form form = new Form();
		form.param("email", email);
		form.param("password", password);
		
		Invocation.Builder ib =	getIB(target);
		
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		UserTO user = response.readEntity(UserTO.class);
			
		return user;
	}
	
	Builder getIB(WebTarget target)
	{
		Invocation.Builder ib =	target.request(MediaType.APPLICATION_JSON_TYPE);
		if(sessionCookie != null)
		{
			ib.cookie(sessionCookie); 
		}
		
		if(app.processorUuid != null)
		{
			ib.header("processor-uuid", app.processorUuid);
		}
		
		return ib;
	}
	
	
	ContainerTO createContainer() throws Exception
	{
		//log.info("... create container ...");
		
		WebTarget target = client.target(host).path("/baltoro/api/bo/createContainer");
		 
		Form form = new Form();
		form.param("name", "customer 1");
		
		Invocation.Builder ib =	getIB(target);
		
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		ContainerTO container = response.readEntity(ContainerTO.class);
		
		//log.info("container : ... "+container.getBaseUuid());
		
		return container;
		
	}
	
	EntityTO createEntity(String email) throws Exception
	{
		WebTarget target = client.target(host).path("/baltoro/api/bo/createEntity");
		String hashedEmail = CryptoUtil.hash(email);
		ReqTO to = new ReqTO();
		to.setProcessorUuid(app.processorUuid);
		to.addField(ReqTOField.HASHED_EMAIL, hashedEmail);
		CryptoUtil.sign(to, app.privateKey);
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(to, MediaType.APPLICATION_JSON_TYPE));
		EntityTO entity = response.readEntity(EntityTO.class);
		return entity;
		
	}
	
	ProcessorTO createProcessor(ContainerTO container, String publicKey) throws Exception
	{
		WebTarget target = client.target(host).path("/baltoro/api/bo/createProcessor");
		
		Form form = new Form();
		form.param("name", "processor");
		form.param("container-uuid", container.uuid);
		form.param("public-key", publicKey);
		
		Invocation.Builder ib =	getIB(target);
		
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		ProcessorTO processor = response.readEntity(ProcessorTO.class);
		
		app.processorUuid = processor.uuid;
		return processor;
		
	}
	
	String setProcessorSession(String processorUuid) throws Exception
	{
		WebTarget target = client.target(host).path("/baltoro/api/bo/setProcessorSession");
		
		Form form = new Form();
		form.param("processor-uuid", processorUuid);
		
		Invocation.Builder ib =	getIB(target);
		
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		processorUuid = response.readEntity(String.class);
		return processorUuid;
		
	}
	
	
	
	
	TxTO createTransaction(String email, String serviceName, String ipAddress, String status) 
	throws Exception
	{
		WebTarget target = client.target(host).path("/baltoro/api/bo/createTransaction");
		String hashedEmail = CryptoUtil.hash(email);
		
		ReqTO to = new ReqTO();
		to.setProcessorUuid(app.processorUuid);
		to.addField(ReqTOField.HASHED_EMAIL, hashedEmail);
		to.addField(ReqTOField.SERVICE_NAME, serviceName);
		to.addField(ReqTOField.IP_ADDRESS, ipAddress);
		to.addField(ReqTOField.STATUS, status);
		
		CryptoUtil.sign(to, app.privateKey);
	
		String unsign = CryptoUtil.decrypt(app.publicKey, to.getSignature());
		if(!unsign.equals(to.getHash()))
		{
			System.out.println("hash:"+to.getHash());
			System.out.println("unsign:"+unsign);
			throw new Exception("hash doesn't match");
		}
		
		
		Invocation.Builder ib =	getIB(target);
		
		Response response = ib.post(Entity.entity(to, MediaType.APPLICATION_JSON_TYPE));
		
		TxTO tx = response.readEntity(TxTO.class);
		
		System.out.println(app.containerUuid+" >>>>>> tx created "+tx.uuid);
		
		return tx;
		
	}
	
	TxTO postTX(String email, String serviceName, String ipAddress, String status) throws Exception
	{
		WebTarget target = client.target(host).path("/baltoro/api/bo/createTransaction");

		
		return null;
	}
	
	<T extends BaseTO> T getBO(String baseUuid, Class<T> type) throws Exception
	{
	
		String url = "/baltoro/api/bo/";
		if(type == ContainerTO.class)
		{
			url = url+"getContainer";
		}
		else if(type == UserTO.class)
		{
			url = url+"getUser";
		}
		else if(type == ProcessorTO.class)
		{
			url = url+"getProcessor";
		}
		
		
		WebTarget target = client.target(host).path(url);
		target = target.queryParam("uuid", baseUuid);
		 
		Invocation.Builder ib =	getIB(target);
		Response response = ib.get();
		BaseTO bo = response.readEntity(type);
		
		return type.cast(bo);
		
	}
	
	
	
	UserTO createUser(String email, String password, ContainerTO container) throws Exception
	{
		
		WebTarget target = client.target(host).path("/baltoro/api/bo/createUser");
		 
		Form form = new Form();
		form.param("email", email);
		form.param("password", password);
		form.param("container-uuid", container.uuid);
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		UserTO user = response.readEntity(UserTO.class);
		return user;
		
	}
	
	BlockTO createBlock(Lock lock, List<TxTO> txList) throws Exception
	{
		
		WebTarget target = client.target(host).path("/baltoro/api/bo/createBlock");
		
		ReqTO to = new ReqTO();
		to.setLockUuid(lock.getUuid());
		to.setProcessorUuid(app.processorUuid);
		to.addField(ReqTOField.NAME, "block ..");
		
		for (TxTO tx : txList)
		{
			to.addTransactionToBlock(tx);
		}
		
		CryptoUtil.sign(to, app.privateKey);
	
		Invocation.Builder ib =	getIB(target);
		
		Response response = ib.post(Entity.entity(to, MediaType.APPLICATION_JSON));
		BlockTO block = response.readEntity(BlockTO.class);
	
		return block;
		
	}
	
	int getOpenTransactionCount() throws Exception
	{
		
		WebTarget target = client.target(host).path("/baltoro/api/bo/getOpenTransactionCount");
		
		Form form = new Form();
		form.param("processor-uuid", app.processorUuid);
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		
		Integer count = response.readEntity(Integer.class);
	
		return count;
		
	}
	

	
	List<TxTO> getOpenTransactions() throws Exception
	{
		WebTarget target = client.target(host).path("/baltoro/api/bo/getOpenTransactions");
		Form form = new Form();
		form.param("processor-uuid", app.processorUuid);
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		List<TxTO> list = response.readEntity(new GenericType<List<TxTO>>(){});
		return list;
		
	}
	
	List<BlockTO> downloadBlocks(String lastBlockUuid) throws Exception
	{
		WebTarget target = client.target(host).path("/baltoro/api/bo/downloadBlocks");
		
		Form form = new Form();
		form.param("last-block-uuid", lastBlockUuid);
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		List<BlockTO> list = response.readEntity(new GenericType<List<BlockTO>>(){});
		return list;
		
	}
	
	Lock getLock () throws Exception
	{
		
		WebTarget target = client.target(host).path("/baltoro/api/bo/getLockForBlockCreation");
		Form form = new Form();
		form.param("processor-uuid", app.processorUuid);
		Invocation.Builder ib =	getIB(target);
		Response response = ib.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
		Lock lock = response.readEntity(Lock.class);
		System.out.println(" >>>>>>>>>>>>>>>>>> "+lock.getUuid());
		return lock;
		
	}
	*/
}

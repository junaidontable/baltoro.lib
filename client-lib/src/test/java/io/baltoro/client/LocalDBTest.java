package io.baltoro.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LocalDBTest
{

	LocalDB db = null;
	ObjectMapper mapper = new ObjectMapper();
	
	
	@Before
	public void setup()
	{
		System.out.println("...... before ...... ");
		Baltoro.init("baltoro", Env.JUNIT);
		Baltoro.register("db", "");
		Baltoro.start();
		db = Baltoro.getDB();
		
	}
	
	@After
	public void tearDown()
	{
		System.out.println("..... finished testing ....");
	}
	
	
	@Test
	public void test1()
	{
		
		TestObj1 obj1 = new TestObj1();
		obj1.setName("obj1");
		db.save(obj1);
		
		
		
		
		for (int i = 0; i < 100; i++)
		{
			TestObj2 obj2 = new TestObj2();
			obj2.setName("obj2-"+i);
			db.save(obj2);
			
			TestObj3 obj3 = new TestObj3();
			obj3.setName("obj3"+i);
			db.save(obj3);
			
		
			db.link(obj1, obj2, obj3);
		}
		
		
		Linked<TestObj2> l = db.getChildren(TestObj2.class, obj1);
		
		System.out.println("l ...... "+l.getCount());
		System.out.println("l ...... "+l.getFirstUuid());
		System.out.println("l ...... "+l.getUuids().size());
		System.out.println("l ...... "+l.getAllAsArray().length);
		System.out.println("l ...... "+l.getAttUuids(l.getFirstUuid()));
		
		
		Linked<TestObj2> l1 = db.getChildren(TestObj2.class, obj1);
		
		System.out.println("l1 ...... "+l1.getCount());
		System.out.println("l1 ...... "+l1.getFirstUuid());
		System.out.println("l1 ...... "+l1.getUuids().size());
		System.out.println("l1 ...... "+l1.getAllAsArray().length);
		System.out.println("l1 ...... "+l1.getAttUuids(l1.getFirstUuid()));
						
		
	}
		
		
}

package io.baltoro.client;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class DBTest extends TestCase
{
    
	private static LocalDB db = LocalDB.instance();
	
	private static String uuidObj1;
	private static String uuidObj2;
	private static String uuidObj3;
	
	
    public DBTest( String testName )
    {
        super( testName );
        
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( DBTest.class );
    }

   
    public void testInsert()
    {
    	System.out.println("............. begin insert test ............. ");
    	
    	TestObj1 obj1 = new TestObj1();
    	obj1.setName("name1");
    	obj1.setAtt1("att11");
    	obj1.setAtt2(12);
    	obj1.setAtt3("att13");
   
    	db.save(obj1);
    	uuidObj1 = obj1.getUuid();
    	System.out.println("obj1 uuid = "+uuidObj1);
    	
    	TestObj2 obj2 = new TestObj2();
    	obj2.setName("name2");
    	obj2.setAtt1("att21");
    	obj2.setAtt2(22);
    	obj2.setAtt3("att23");
    	
    	db.save(obj2);
    	uuidObj2 = obj2.getUuid();
    	System.out.println("obj2 uuid = "+uuidObj2);
    	
    	TestObj3 obj3 = new TestObj3();
    	obj3.setName("name3");
    	obj3.setAtt1("att31");
    	obj3.setAtt2(32);
    	obj3.setAtt3("att33");
    	
    	db.save(obj3);
    	uuidObj3 = obj3.getUuid();
    	System.out.println("obj3 uuid = "+uuidObj3);
    	
        assertTrue( true );
    }
    
    
    public void testLoad()
    {
    	
    	db.cleanData();
    	testInsert();
    	System.out.println("............. begin read test ............. "+uuidObj1);
    	
    	
    	TestObj1 obj1 = db.get(uuidObj1, TestObj1.class);
    	
    	assertEquals("name1", obj1.getName());
    	assertEquals("att11", obj1.getAtt1());
    	assertEquals(12, obj1.getAtt2());
    	assertEquals("att13", obj1.getAtt3());
    	
    	TestObj2 obj2 = db.get(uuidObj2, TestObj2.class);
    	
    	
    	assertEquals("name2", obj2.getName());
    	assertEquals("att21", obj2.getAtt1());
    	assertEquals(22, obj2.getAtt2());
    	assertEquals("att23", obj2.getAtt3());
    	
    	TestObj3 obj3 = db.get(uuidObj3, TestObj3.class);
    	
    	
    	assertEquals("name3", obj3.getName());
    	assertEquals("att31", obj3.getAtt1());
    	assertEquals(32, obj3.getAtt2());
    	assertEquals("att33", obj3.getAtt3());
    	
    
    }
    
    
    public void testFind()
    {
    	db.cleanData();
    	testInsert();
    	
    	System.out.println("............. begin find test ............. ");
    	
    	TestObj1 obj1 = db.getByName("name1", TestObj1.class);
    	assertNotNull(obj1);
    	
    	assertEquals("name1", obj1.getName());
    	assertEquals("att11", obj1.getAtt1());
    	assertEquals(12, obj1.getAtt2());
    	assertEquals("att13", obj1.getAtt3());
    	
    	TestObj2 obj2 = db.getByName("name2", TestObj2.class);
    	assertNotNull(obj2);
    	
    	assertEquals("name2", obj2.getName());
    	assertEquals("att21", obj2.getAtt1());
    	assertEquals(22, obj2.getAtt2());
    	assertEquals("att23", obj2.getAtt3());
    	
    	TestObj3 obj3 = db.getByName("name3", TestObj3.class);
    	assertNotNull(obj3);
    	assertEquals("name3", obj3.getName());
    	assertEquals("att31", obj3.getAtt1());
    	assertEquals(32, obj3.getAtt2());
    	assertEquals("att33", obj3.getAtt3());
    	
    }
    
    
    public void testLinkCreate()
    {
    	db.cleanData();
    	testInsert();
    	
    	System.out.println("............. begin link create test ............. ");
    	
    	
    	
    	TestObj1 obj1 = db.getByName("name1", TestObj1.class);
    	assertNotNull(obj1);
    	
    	TestObj2 obj2 = db.getByName("name2", TestObj2.class);
    	assertNotNull(obj2);
    	
    	
    	String linkUuid = db.link(obj1.getBaseUuid(), obj2.getBaseUuid());
    	
    	System.out.println("link uuid = "+linkUuid);
    	
    	assertNotNull(linkUuid);
    	
    	
    	for (int i = 0; i < 10; i++)
		{
    		TestObj3 obj = new TestObj3();
    		obj.setName("name3"+i);
    		obj.setAtt1("att3"+i);
    		obj.setAtt2(i);
    		obj.setAtt3("att3"+i);
    		db.save(obj);
    		
    		linkUuid = db.link(obj1.getBaseUuid(), obj.getBaseUuid());
    		
    		System.out.println("link obj1 to "+obj.getName()+" = "+linkUuid);
        	
		}
    	
    }
    
    public void testLinkGet()
    {
    	db.cleanData();
    	testInsert();
    	testLinkCreate();
    	
    	System.out.println("............. begin link get test ............. ");
    	
    	List<TestObj2> list = db.getChildren(TestObj2.class, uuidObj1);
    	
    	
    	System.out.println("list size : "+list.size());
    	assertEquals(1, list.size());
    	
    	TestObj2 obj2 = list.get(0);
    	
    	System.out.println("object2 : "+obj2);
    	
    	assertEquals(TestObj2.class, obj2.getClass());
    	
    	
    	List<TestObj3> list1 = db.getChildren(TestObj3.class, uuidObj1);
    	
    	
    	System.out.println("list size obj3 : "+list1.size());
    	assertEquals(10, list1.size());
    	
    	
    }
    
    public void testLinkGet2()
    {
    	db.cleanData();
    	testInsert();
    	
    	System.out.println("............. begin link get test ....... 2 ...... ");
    	
    	TestObj1 obj1 = db.getByName("name1", TestObj1.class);
    	assertNotNull(obj1);
    	
    	TestObj2 obj2 = db.getByName("name2", TestObj2.class);
    	assertNotNull(obj2);
    	
    	TestObj3 obj3 = db.getByName("name3", TestObj3.class);
    	assertNotNull(obj2);
    	
    	
    	String linkUuid = db.link(obj1.getBaseUuid(), obj2.getBaseUuid(), obj3);
    	
    	System.out.println(linkUuid);
    	
    	List<TestObj2> list = null;//db.findLinked(TestObj2.class, obj1, obj3);
    	
    	System.out.println("obj found = "+list.get(0));
    	
    	
    }
    
    
    public void testLinkGet3()
    {
    	db.cleanData();
    	testInsert();
    	
    	System.out.println("............. begin link get test ....... 2 ...... ");
    	
    	TestObj1 obj1 = db.getByName("name1", TestObj1.class);
    	assertNotNull(obj1);
    	
    	TestObj2 obj2 = db.getByName("name2", TestObj2.class);
    	assertNotNull(obj2);
    	
    	TestObj3 obj3 = db.getByName("name3", TestObj3.class);
    	assertNotNull(obj2);
    	
    	
    	TestObj2 obj22 = new TestObj2();
    	obj22.setName("name2-2");
    	db.save(obj22);
    	
    	TestObj2 obj23 = new TestObj2();
    	obj23.setName("name2-3");
    	db.save(obj23);
    	
    	String linkUuid = db.link(obj1.getBaseUuid(), obj22.getBaseUuid());
    	System.out.println(linkUuid);
    	
    	
    	
    	
    	List<TestObj2> list = db.getChildren(TestObj2.class,obj1);
    	
    	System.out.println(" ------------> linkType22 obj found = "+list.get(0));
    	
    	list = db.getChildren(TestObj2.class,obj1);
    	
    	System.out.println("-------------> linkType23 obj found = "+list.get(0));
    	
    	
    }
    
    
}

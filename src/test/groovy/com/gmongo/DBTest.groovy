package com.gmongo

import com.mongodb.DBCollection
import com.mongodb.DBObject

class DBTest extends IntegrationTestCase {
	
	def db
	
	void setUp() {
		super.setUp()
		db = mongo.db
	}
	
	void testGetCollecion() {
		assert db.foo instanceof DBCollection
		assert db.bar instanceof DBCollection
		assert db.name instanceof DBCollection
	}
	
	void testGetLastError() {
		assertTrue db.getLastError() instanceof DBObject
	}

}
package com.gmongo

import com.gmongo.internal.Patcher

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject

class DBTest extends IntegrationTestCase {
	
	def db
	
	void setUp() {
		super.setUp()
		db = mongo.getDB(DB_NAME)
	}
	
	void testGetCollection() {
		def c =  db.getCollection('foo_bar')
		assert c.hasProperty(Patcher.PATCH_MARK)
	}
	
	void testGetCollectionFromFull() {
		def c = db.getCollectionFromFull('gmongo_test.foo_baz')
		assert c.hasProperty(Patcher.PATCH_MARK)
	}
	
	void testGetCollectionFromString() {
		def c = db.getCollectionFromString('bar_baz')
		assert c.hasProperty(Patcher.PATCH_MARK)
	}
	
	void testGetCollecionGroovy() {
		assert db.foo instanceof DBCollection
		assert db.foo.hasProperty(Patcher.PATCH_MARK)
		assert db.bar instanceof DBCollection
		assert db.name instanceof DBCollection
	}
	
	void _testGetName() {
		assertEquals DB_NAME, db.getName()
	}
	
	void testGetLastError() {
		assertTrue db.getLastError() instanceof DBObject
	}
	
	void testExecuteCommand() {
		def result = db.command([ismaster: 1])
		assertTrue result.ok()
		assertEquals 1, result.ismaster
	}
	
	void testExecuteCommandString() {
		def result = db.command('ismaster')
		assertTrue result.ok()
		assertEquals 1, result.ismaster
	}
	
	void testInRequestMock() {
		def started, ended = false
		db.metaClass.requestStart { started = true }
		db.metaClass.requestDone  { ended = true }
		db.inRequest {->
			// Do something consistently
		}
		assertTrue started && ended
	}
	
	void testInRequest() {
		db.inRequest {->
			db.foo.insert(bar: 10)
			assertEquals 1, db.foo.count()
		}
	}
}
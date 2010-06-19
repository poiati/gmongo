package com.gmongo

class DBCursorTest extends IntegrationTestCase {
	
	def db
	
	void setUp() {
		super.setUp()
		db = mongo.getDB(DB_NAME)
		db.foo.drop()
	}
	
	void testIteration() {
		_insert()
		def cursor = db.foo.find()
		def times = 0
		for (def doc in cursor)
			times++
		assertEquals 3, times
	}

	void testEach() {
		_insert()
		def times = 0
		db.foo.find().each {
			times++
		}
		assertEquals 3, times
	}
	
	def _insert() {
		db.foo << [ [foo: 1], [bar: 2], [baz: 3] ]
	}
}
package com.gmongo

import com.mongodb.BasicDBObject
import com.mongodb.DBObject

class DBCollectionTest extends IntegrationTestCase {
	
	def db
	def coll
	
	void setUp() {
		super.setUp()
		db = mongo.db
		coll = db.getCollection('foo')
		coll.drop()
	}
	
	void tearDown() {
		coll.drop()
	}
	
	void testInsert() {
		db.foo.insert([bar: 1])
		assert db.foo.count() == 1
		assert coll.findOne().bar == 1
	}
	
	void testInsertEmbedded() {
		db.foo.insert([bar: [foo: 1]])
		assert db.foo.count() == 1
		assert coll.findOne().bar.foo == 1
	}
	
	void testInsertEmbeddedSimpleList() {
		db.foo.insert(bar: ['foo', 'baz', 1])
		assert db.foo.count() == 1
		assert coll.findOne().bar[0] == 'foo'
		assert coll.findOne().bar[1] == 'baz'
		assert coll.findOne().bar[2] == 1
	}
	
	void testInsertEmbeddedList() {
		db.foo.insert([bar: [[foo: 1], [baz: 2]]])
		assert db.foo.count() == 1
		assert coll.findOne().bar.get(0).foo == 1
		assert coll.findOne().bar.get(1).baz == 2
		assert coll.findOne().bar[0].foo == 1
		assert coll.findOne().bar[1].baz == 2
	}
	
	void testInsertOperator() {
		db.foo << [bar: 1]
		assert db.foo.count() == 1
	}
	
	void testMultipleInserts() {
		db.foo.insert(bar: 1)
		db.foo.insert(bar: 5)
		db.foo << [bar: 10]
		assert db.foo.count() == 3
		coll.find().each {
			assert [1, 5, 10].contains(it.bar)
		}
	}
	
	void testInsertList() {
		db.foo.insert([[key: 1], [key: 2], [foo: 'bar']])
		assertEquals 3, db.foo.count()
	}
	
	void testInsertArray() {
		DBObject[] objs = new DBObject[2]
		objs[0] = [key: 1] as BasicDBObject
		objs[1] = [key: 2] as BasicDBObject
		db.foo.insert objs
		assertEquals 2, db.foo.count()
	}
	
	void testFind() {
		_insert()
		assert !db.foo.find(key: 'Bar')
		assert  db.foo.find(key: 'Foo').count() == 1
	}
	
	void testFindOne() {
		_insert(['Bar', 'Foo'])
		def bar = db.foo.findOne()
		assertNotNull bar
		assertEquals 'Bar', bar.key
		def foo = db.foo.findOne(key: 'Foo')
		assertNotNull foo
		assertEquals 'Foo', foo.key
	}
	
	void testRename() {
		_insert()
		def another = coll.rename('justAnotherFooInTheWall')
		assertEquals 'Foo', another.findOne().key
		assertEquals another, db.justAnotherFooInTheWall
		db.justAnotherFooInTheWall.drop()
	}
	
	void testRemove() {
		_insert()
		assertEquals 1, coll.count()
		db.foo.remove(key: 'Foo')
		assertEquals 0, coll.count()
	}
	
	void testRemoveRightShift() {
		_insert()
		assertEquals 1, coll.count()
		db.foo >> [key: 'Foo']
		assertEquals 0, coll.count()
	}
	
	void testSave() {
		def id = '0123'
		coll.insert([_id: id, key: 'foo'] as BasicDBObject)
		assertEquals 1, coll.count()
		db.foo.save(_id: id, key: 'bar')
		assertEquals 1, coll.count()
		assertEquals 'bar', coll.findOne().key
	}
	
	void testCount() {
		//TODO: Check Bug
		//assertEquals 0, db.foo.count()
		_insert()
		assertEquals 1, db.foo.count(key: 'Foo')
		assertEquals 1, db.foo.count()
		assertEquals 0, db.foo.count(key: 'Bar')
	}
	
	void testGetFullName() {
		assert db.foo.getFullName().endsWith('foo')
		assert db.foo.fullName.endsWith('foo')
	}
	
	void testUpdate() {
		_insert()
		db.foo.update([key: 'Bar'], [updated: true])
		assertEquals null, coll.findOne().updated
		def p = [key: 'Bar'] as BasicDBObject
		assertNull db.foo.findOne(p)
		// TODO: WTF ?? org.codehaus.groovy.runtime.wrappers.PojoWrapper
	    // assertNull db.foo.findOne([key: 'Bar'] as BasicDBObject)
		db.foo.update([key: 'Foo'], [updated: true])
		assertEquals true, coll.findOne().updated
	}
	
	void testUpdateUpsert() {
		assertEquals 0, coll.count()
		db.foo.update([key: 'Foo'], [key: 'Bar'], true, false)
		assertEquals 1, coll.count()
		db.foo.update([key: 'Foo2'], [key: 'Bar'], true)
		assertEquals 2, coll.count()
	}
	
	void testUpdateUpsertMulti() {
		_insert(['Foo', 'Foo', 'Foo'])
		assertEquals 3, coll.count()
		db.foo.update([key: 'Foo'], [$set: [key: 'Bar']], false, true)
		def times = 0
		db.foo.find().each {
			assertEquals 'Bar', it.key 
			times++
		}
		assertEquals 3, times
	}
	
	void testUpdateUpsertMulti2() {
		_insert(['Foo', 'Foo', 'Foo'])
		assertEquals 3, coll.count()
		db.foo.updateMulti([key: 'Foo'], [$set: [key: 'Bar']])
		def times = 0
		db.foo.find().each {
			assertEquals 'Bar', it.key 
			times++
		}
		assertEquals 3, times
	}
	
	def _insert(keys=['Foo']) {
		keys.each {
			coll.insert([key: it] as BasicDBObject)
		}
	}
}
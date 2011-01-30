package com.gmongo

import com.gmongo.internal.Patcher

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.BasicDBList

class DBCollectionTest extends IntegrationTestCase {

  def db
  def coll

  void setUp() {
    super.setUp()
    db = mongo.getDB(DB_NAME)
    coll = db.getCollection('foo')
    coll.drop()
    db.justAnotherFooInTheWall.drop()
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
    assert	db.foo.find(key: 'Foo').count() == 1
  }

  void testFindFields() {
    db.foo << [foo: 10, bar: 20]
    def c = db.foo.find([:], [foo: 1])
    def obj = c.next()
    assertNull obj.bar
    assertEquals 10, obj.foo 
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

  void testFindOneFields() {
    db.foo << [foo: 10, bar: 20]
    def obj = db.foo.findOne([:], [bar: 1])
    assertNull obj.foo
    assertEquals 20, obj.bar
  }

  void testRename() {
    _insert()
    def another = coll.rename('justAnotherFooInTheWall')
    assertEquals 'Foo', another.findOne().key
    assertEquals another, db.justAnotherFooInTheWall
    another.insert([bar: 20])
    assertEquals 2, another.count()
    assert another.hasProperty(com.gmongo.internal.Patcher.PATCH_MARK)
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
    assertEquals 0, db.foo.count()
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
    // TODO: 1.7.2 BUG, test in 1.7.3
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

  void testDistinct() {
    _insert(['Foo', 'Bar', 'Baz', 'Foo'])
    assertEquals 4, db.foo.count()
    assertEquals 3, db.foo.distinct('key').size()
  }

  void testDistinctQuery() {
    db.foo.insert([[key: 'Foo', n: 1], [key: 'Foo', n: 1], [key: 'Bar', n: 2]])
    assertEquals 3, db.foo.count()
    assertEquals 1, db.foo.distinct('key', [n: 1]).size()
  }

  void testApply() {
    def o = [k: 'foo']
    def id = db.foo.apply(o)
    assertEquals id, o._id
  }

  void testCreateIndex() {
    _insert()
    assertEquals 1, db.foo.indexInfo.size()
    db.foo.createIndex([key: 1])
    assertEquals 2, db.foo.indexInfo.size()
  }

  void testEnsureIndex() {
    _insert()
    assertEquals 1, db.foo.indexInfo.size()
    db.foo.ensureIndex([key: 1])
    assertEquals 2, db.foo.indexInfo.size()
  }

  void testEnsureIndexOptions() {
    _insert()
    assertEquals 1, db.foo.indexInfo.size()
    db.foo.ensureIndex([key: 1], [unique: true])
    assertEquals 2, db.foo.indexInfo.size()
    assertTrue db.foo.indexInfo[1].unique
  }

  void testEnsureIndexName() {
    _insert()
    assertEquals 1, db.foo.indexInfo.size()
    db.foo.ensureIndex([key: 1], 'fooi')
    assertEquals 2, db.foo.indexInfo.size()
    assertEquals 'fooi', db.foo.indexInfo[1].name
  }

  void testEnsureIndexUniqueName() {
    _insert()
    assertEquals 1, db.foo.indexInfo.size()
    db.foo.ensureIndex([key: 1], 'fooi', true)
    assertEquals 2, db.foo.indexInfo.size()
    assertEquals 'fooi', db.foo.indexInfo[1].name
    assertTrue db.foo.indexInfo[1].unique
  }

  void testDropIndexName() {
    _insert()
    db.foo.ensureIndex([key: 1], 'fooi', true)
    assertEquals 2, db.foo.indexInfo.size()
    db.foo.dropIndex("fooi")
    assertEquals 1, db.foo.indexInfo.size()
  }

  void testDropIndex() {
    _insert()
    db.foo.ensureIndex([key: 1])
    assertEquals 2, db.foo.indexInfo.size()
    db.foo.dropIndex([key: 1])
    assertEquals 1, db.foo.indexInfo.size()
  }

  void testDropIndexes() {
    _insert()
    db.foo.ensureIndex([key: 1])
    assertEquals 2, db.foo.indexInfo.size()
    db.foo.dropIndexes()
    assertEquals 1, db.foo.indexInfo.size()
  }

  void testDropIndexesName() {
    _insert()
    db.foo.ensureIndex([key: 1], 'bar')
    assertEquals 2, db.foo.indexInfo.size()
    db.foo.dropIndexes('bar')
    assertEquals 1, db.foo.indexInfo.size()
  }

  void testGetCount() {
    _insert(['Foo', 'Bar'])
    assertEquals 2, db.foo.getCount()
  }

  void testGetCountQuery() {
    _insert(['Foo', 'Bar'])
    assertEquals 1, db.foo.getCount([key: 'Bar'])
  }

  void testSetHintFields() {
    _insert()
    db.foo.setHintFields([[key: true]])
  }

  void testGroup() {
    db.foo.insert([[foo: 10, bar: 1, baz: 100], [foo: 20, bar: 2, baz: 200], 
                   [foo: 10, bar: 1, baz: 300], [foo: 20, bar: 1, baz: 300]])
    def g = db.foo.group([foo: true], [bar: 1], [count: 0], "function(obj, prev) { prev.count += obj.baz }")
    assertEquals 400, g[0].count
    assertEquals 300, g[1].count
  }

  void testCollectionTruth() {
    if (db.foo) assert false
    _insert()
    if (db.foo) return
    assert false
  }

  void testMissingMethod() {
    def msg = shouldFail(MissingMethodException) {
      db.fooBar.blabla([baz: "foo"])
    }
    def msg2 = shouldFail(MissingMethodException) {
      db.fooBar.bla("foo")
    }
    def msg3 = shouldFail(MissingMethodException) {
      db.fooBar.insert("bar")
    }
    assert msg.contains("blabla")
    assert msg2.contains("bla")
    assert msg3.contains("insert")
    assert msg3.contains("String")
  }

  void testGetDB() {
    def _db = db.foo.getDB()
    assert _db.hasProperty(Patcher.PATCH_MARK)
  }

  def _insert(keys=['Foo']) {
    keys.each {
      coll.insert([key: it] as BasicDBObject)
    }
  }
}
package com.gmongo

import com.gmongo.internal.Patcher

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject

class DBTest extends IntegrationTestCase {

  void setUp() {
    super.setUp()
    db.dropDatabase()
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

  void testGetCollectionGroovy() {
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
    assertEquals true, result.ismaster
  }

  void testExecuteCommandString() {
    def result = db.command('ismaster')
    assertTrue result.ok()
    assertEquals true, result.ismaster
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

  void testInRequestMockException() {
    def started, ended = false
    db.metaClass.requestStart { started = true }
    db.metaClass.requestDone  { ended = true }
    try {
      db.inRequest {->
        throw new RuntimeException("Opss!!!")
      }
    } catch (RuntimeException ex) {
      assertEquals "Opss!!!", ex.message
    }
      assertTrue started && ended
  }

  void testCollectionExists() {
    assertFalse db.collectionExists('baz')
    db.baz.insert(foo: 10)
    assertTrue db.collectionExists('baz')
  }

  void testCreateCollection() {
    def c = db.createCollection('foo', [capped: true, size: 100000])
    assert c.hasProperty(Patcher.PATCH_MARK)
    assert db.foo.hasProperty(Patcher.PATCH_MARK)
  }

  void testMissingMethod() {
    def msg = shouldFail(MissingMethodException) {
      db.foo()
    }

    def msg2 = shouldFail(MissingMethodException) {
      db.foo('bar')
    }

    assert msg.contains("foo")
    assert msg2.contains("foo")
    assert msg2.contains("bar")
  }

  void testInRequest() {
    db.inRequest {->
      db.foo.insert(bar: 10)
      assertEquals 1, db.foo.count()
    }
  }
}
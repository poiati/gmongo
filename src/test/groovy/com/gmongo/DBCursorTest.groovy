package com.gmongo

import com.gmongo.internal.Patcher

class DBCursorTest extends IntegrationTestCase {

  void setUp() {
    super.setUp()
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
  
  void testSort() {
    _insert()
    def sortedAsc = db.foo.find().sort([order: 1])
    assert sortedAsc.hasProperty(Patcher.PATCH_MARK)
    assert 2 == sortedAsc.next().bar
    assert 3 == sortedAsc.next().baz
    assert 1 == sortedAsc.next().foo
    
    def sortedDesc = db.foo.find().sort([order: -1])
    assert sortedDesc.hasProperty(Patcher.PATCH_MARK)
    assert 1 == sortedDesc.next().foo
    assert 3 == sortedDesc.next().baz
    assert 2 == sortedDesc.next().bar
  }
  
  void testSortFindWithQuery() {
    _insert()
    def sortedAsc = db.foo.find([order: [$lt: 4]]).sort([order: 1])
    assert sortedAsc.hasProperty(Patcher.PATCH_MARK)
    assert 2 == sortedAsc.next().bar
    assert 3 == sortedAsc.next().baz
    
    def sortedDesc = db.foo.find([order: [$lt: 4]]).sort([order: -1])
    assert sortedDesc.hasProperty(Patcher.PATCH_MARK)
    assert 3 == sortedDesc.next().baz
    assert 2 == sortedDesc.next().bar
  }
  
  void testHint() {
    _insert()
    def cursor = db.foo.find().hint([$natural : 1])
    assert cursor.hasProperty(Patcher.PATCH_MARK)
    assert 3 == cursor.count()
  }
  
  void testLimit() {
    _insert()
    def cursor = db.foo.find().limit(1)
    assert cursor.hasProperty(Patcher.PATCH_MARK)
    assert 1 == cursor.size()
  }
  
  void testCopy() {
    _insert()
    def cursor = db.foo.find()
    assert cursor.hasProperty(Patcher.PATCH_MARK)
    assert 3 == cursor.count()
    def copy = cursor.copy()
    assert copy.hasProperty(Patcher.PATCH_MARK)
    assert 3 == copy.count()
  }
  
  def _insert() {
    db.foo << [ [foo: 1, order: 4], [bar: 2, order: 1], [baz: 3, order: 3] ]
  }
}
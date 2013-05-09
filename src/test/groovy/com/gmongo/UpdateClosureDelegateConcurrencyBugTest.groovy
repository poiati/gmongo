package com.gmongo

import com.mongodb.WriteConcern

class UpdateClosureDelegateConcurrencyBugTest extends IntegrationTestCase {
  
  void setUp() {
    super.setUp()
    mongo.setWriteConcern(WriteConcern.NORMAL)
    db.dropDatabase()
  }

  void tearDown() {
    db.dropDatabase()
  }

  void testBehaviorOnHighLoad() {
    def startedAt = System.currentTimeMillis()
    def runUntil = startedAt + (100 * 60)

    def threads = []
    6.times {
      def thread = Thread.start {
        while (true) {
          db.one.update([nomatch: 1], [collection: 'one'], true)
          db.two.update([nomatch: 1], [collection: 'two'], true)

          if (System.currentTimeMillis() > runUntil)
            break;
        }
      }

      threads << thread
    }

    threads.each { it.join() }

    println db.one.count() + " elements in one."
    println db.two.count() + " elements in two."

    assertNull "Found 'two' in the 'one' collection", db.one.findOne(collection: 'two')
    assertNull "Found 'one' in the 'two' collection", db.two.findOne(collection: 'one')
  }
}

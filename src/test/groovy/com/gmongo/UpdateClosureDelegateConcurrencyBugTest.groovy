package com.gmongo

import com.mongodb.WriteConcern

class UpdateClosureDelegateConcurrencyBugTest extends IntegrationTestCase {
  
  void setUp() {
    super.setUp()
    mongo.setWriteConcern(WriteConcern.NORMAL)
    db.dropDatabase()
  }

  void testBehaviorOnHighLoad() {
    def startedAt = System.currentTimeMillis()
    def runUntil = startedAt + (100 * 60)

    def threads = []
    6.times {
      def thread = Thread.start {
        while (true) {
          db.foo.update([nomatch: 1], [collection: 'foo'], true)
          db.bar.update([nomatch: 1], [collection: 'bar'], true)

          if (System.currentTimeMillis() > runUntil)
            break;
        }
      }

      threads << thread
    }

    threads.each { it.join() }

    println db.foo.count() + " elements in foo."
    println db.bar.count() + " elements in bar."

    assertNull "Found 'bar' in the 'foo' collection", db.foo.findOne(collection: 'bar')
    assertNull "Found 'foo' in the 'bar' collection", db.bar.findOne(collection: 'foo')
  }
}

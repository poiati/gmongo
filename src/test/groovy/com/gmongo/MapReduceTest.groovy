package com.gmongo

import java.util.Random as Rand

class MapReduceTest extends GroovyTestCase {

  void testMapReduce() {

    def mongo = new GMongo()
    def db = mongo.getDB("gmongo")

    def words = ['foo', 'bar', 'baz']
    def rand  = new Rand()		

    1000.times { 
      db.words << [word: words[rand.nextInt(3)]]
    }

    assert db.words.count() == 1000

    def result = db.words.mapReduce(
      """
      function map() {
        emit(this.word, {count: 1})
      }
      """,
      """
      function reduce(key, values) {
        var count = 0
        for (var i = 0; i < values.length; i++)
        count += values[i].count
        return {count: count}
      }
      """,
      "mrresult",
      [:] // No Query
    )

    assert db.mrresult.count() == 3
    assert db.mrresult.find()*.value*.count.sum() == 1000
  }

  void setUp() {
    new GMongo().getDB("gmongo").dropDatabase()
  }

  void tearDown() {
    new GMongo().getDB("gmongo").dropDatabase()
  }
}
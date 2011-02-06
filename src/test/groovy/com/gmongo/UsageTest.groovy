package com.gmongo

//TODO: Update to 0.6
class UsageTest extends GroovyTestCase {

  void testUsage() {
    // Instantiate a com.gmongo.GMongo object instead of com.mongodb.Mongo
    // The same constructors and methods are available here
    def mongo = new GMongo()

    // Get a db reference in the old fashion way
    def db = mongo.getDB("gmongo")

    // Collections can be accessed as a db property (like the javascript API)
    assert db.myCollection instanceof com.mongodb.DBCollection
    // They also can be accessed with array notation 
    assert db['my.collection'] instanceof com.mongodb.DBCollection

    // Insert a document
    db.languages.insert([name: 'Groovy'])
    // A less verbose way to do it
    db.languages.insert(name: 'Ruby')
    // Yet another way
    db.languages << [name: 'Python']

    // Insert a list of documents
    db.languages << [[name: 'Javascript', type: 'prototyped'], [name: 'Ioke', type: 'prototyped']]

    def statics = ['Java', 'C', 'VB']

    statics.each {
      db.languages << [name: it, type: 'static']
    }

    // Finding the first document
    def lang = db.languages.findOne()
    assert lang.name == 'Groovy'
    // Set a new property
    lang.site = 'http://groovy.codehaus.org/'
    // Save the new version
    db.languages.save lang

    assert db.languages.findOne(name: 'Groovy').site == 'http://groovy.codehaus.org/'

    // Counting the number of documents in the collection
    assert db.languages.find(type: 'static').count() == 3

    // Another way to count
    assert db.languages.count(type: 'prototyped') == 2

    // Updating a document using the '$set' operator
    db.languages.update([name: 'Python'], [$set: [paradigms: ['object-oriented', 'functional', 'imperative']]])

    assert 3 == db.languages.findOne(name: 'Python').paradigms.size()

    // Using upsert
    db.languages.update([name: 'Haskel'], [$set: [paradigms: ['functional']]], true)

    assert db.languages.findOne(name: 'Haskel')

    // Removing some documents
    db.languages.remove(type: 'prototyped')
    assert 0 == db.languages.count(type: 'prototyped')

    // Removing all documents
    db.languages.remove([:])
    assert 0 == db.languages.count()

    // To ensure complete consistency in a session use DB#inRequest
    // It is analogous to user DB#requestStarted and DB#requestDone
    db.inRequest {
      db.languages.insert(name: 'Objective-C')
      assert 1 == db.languages.count(name: 'Objective-C')
    }
  }
  
  void testSortingUsage() {
    def mongo = new GMongo()
    def db = mongo.getDB("gmongo")
    
    db.example.drop()

    100.times {
        db.example << [time: it, random: (Integer)(Math.random() * 100)]
    }

    def at = 0, total = db.example.find().count()

    while (at < total) {
        println "At page: ${at / 10}\n"
        db.example.find().limit(10).skip(at).sort(random: 1).each {
            println "\t-- ${it}"
        }
        println "\n--------------------------"
        at += 10
    }
  }

  void setUp() {
    new GMongo().getDB("gmongo").dropDatabase()
  }

  void tearDown() {
    new GMongo().getDB("gmongo").dropDatabase()
  }
}
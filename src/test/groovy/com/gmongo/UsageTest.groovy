package com.gmongo

class UsageTest extends GroovyTestCase {
	
	void testUsage() {
		def mongo = new GMongo()
		def db = mongo.db
		
		// Collections can be accessed as an db property (like the javascript API)
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
			db['languages'] << [name: it, type: 'static']
		}
		
		// Finding the first document of the collection
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
		
		db.books.insert(name: 'MongoDB in Action', year: 2010, authors: ['Paulo Poiati', 'Larry Page'])
		
		assert db.books.findOne().authors[1] == 'Larry Page'
	}
	
	void setUp() {
		new GMongo().dropDatabase()
	}
	
	void tearDown() {
		new GMongo().dropDatabase()
	}
	
}
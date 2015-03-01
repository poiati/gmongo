GMongo 
======

[![Build Status](https://travis-ci.org/poiati/gmongo.png)](https://travis-ci.org/poiati/gmongo)

The goal of this project is to provide a more simple, easy to use and less verbose API to work with mongodb using the Groovy programming language.

More information can be found here: http://blog.paulopoiati.com/2010/06/20/gmongo-0-5-released/.

# Usage

com.gmongo.GMongo doesn't extends com.mongodb.Mongo. It delegate all methods calls to a Mongo instance. If
you need to get the Mongo reference just call com.gmongo.GMongo#getMongo.

You can also use `com.gmongo.GMongoClient`. It has the same constructors
as `com.mongodb.MongoClient`. For example, to connect to a MongoDB instance with auth enabled.

```groovy
@Grab(group='com.gmongo', module='gmongo', version='1.3')

import com.gmongo.GMongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress

credentials = MongoCredential.createMongoCRCredential('username', 'database', 'password' as char[])

client = new GMongoClient(new ServerAddress(), [credentials])
```

Auth is only available in GMongo 1.3 and above.

(*If you are using Mongodb 3.x you need to use `MongoCredential#createCredential` instead*)

Sample:

```groovy
// To download GMongo on the fly and put it at classpath
@Grab(group='com.gmongo', module='gmongo', version='1.0')
import com.gmongo.GMongo
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
```

## Sorting and Pagination
```groovy
@Grab(group='com.gmongo', module='gmongo', version='1.0')
import com.gmongo.GMongo

def mongo = new GMongo()
def db = mongo.getDB("gmongo")

// Make sure that the collection is empty
db.example.drop()

// Insert 100 documents with any random value
100.times {
    db.example << [time: it, random: (Integer)(Math.random() * 100)]
}

def at = 0

// Find out how many documents are in the collection
def total = db.example.find().count()

// Sort the documents by the 'random' property ascending and Paginate over it 10 by 10
while (at < total) {
    println "At page: ${at / 10}\n"
    db.example.find().limit(10).skip(at).sort(random: 1).each {
        println "\t-- ${it}"
    }
    println "\n--------------------------"
    at += 10
}
```
    
## MapReduce
```groovy
@Grab(group='com.gmongo', module='gmongo', version='1.0')
import com.gmongo.GMongo

def mongo = new GMongo()
def db = mongo.getDB("gmongo")

def words = ['foo', 'bar', 'baz']
def rand  = new Random()		

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
```
## Grouping

Grouping can also be achieved. Example:

```groovy
@Grab("com.gmongo:gmongo:1.0")
import com.gmongo.GMongo

def gmongo = new GMongo("localhost:27017")

def db = gmongo.getDB("test")

db.clicks.drop()

db.clicks.insert(day: 1, total: 10)
db.clicks.insert(day: 1, total: 14)
db.clicks.insert(day: 2, total: 45)
db.clicks.insert(day: 1, total:  9)
db.clicks.insert(day: 3, total: 32)
db.clicks.insert(day: 2, total: 11)
db.clicks.insert(day: 3, total: 34)

def result = db.clicks.group([day: true], [:], [count: 0], "function(doc, out) { out.count += doc.total }")

// Will output [[day:1.0, count:33.0], [day:2.0, count:56.0], [day:3.0, count:66.0]]
println result
```

And a more advanced grouping using 'keyf':

```groovy
@Grab("com.gmongo:gmongo:1.0")
import com.gmongo.GMongo

def gmongo = new GMongo("localhost:27017")

def db = gmongo.getDB("test")

db.clicks.drop()

db.clicks.insert(day: 1, total: 10)
db.clicks.insert(day: 1, total: 14)
db.clicks.insert(day: 2, total: 45)
db.clicks.insert(day: 1, total:  9)
db.clicks.insert(day: 3, total: 32)
db.clicks.insert(day: 2, total: 11)
db.clicks.insert(day: 3, total: 34)

def keyf = "function(clicks) { return clicks.day % 2 ? { odd: true } : { even: true } }"

def command = ['$keyf': keyf, cond: [:], initial: [count: 0], $reduce: "function(doc, out) { out.count += doc.total }"]

def result = db.clicks.group(command)

// Will output [[odd:true, count:99.0], [even:true, count:56.0]]
println result  
```

# Aggregation

This features is only available in version 1.0 or greater.

The simple example below will get the name of all the cities with
population greater or equal 10.000 and sort it.

```groovy
@Grab("com.gmongo:gmongo:1.0")
import com.gmongo.GMongo

def gmongo = new GMongo("localhost:27017")

def db = gmongo.getDB("test")

db.zipcodes.drop()
db.zipcodes << ["city": "ACMAR", "loc": [-86.51557F, 33.584132F], "pop": 6055, "state": "AL", "_id": "35004"]
db.zipcodes << ["city": "ADAMSVILLE", "loc": [-86.959727F, 33.588437F], "pop": 10616, "state": "AL", "_id": "35005"]
db.zipcodes << ["city": "ADGER", "loc": [-87.167455F, 33.434277F], "pop": 3205, "state": "AL", "_id": "35006"]
db.zipcodes << ["city": "KEYSTONE", "loc": [-86.812861F, 33.236868F], "pop": 14218, "state": "AL", "_id": "35007"]
db.zipcodes << ["city": "NEW SITE", "loc": [-85.951086F, 32.941445F], "pop": 19942, "state": "AL", "_id": "35010"]

def aggrOutput = db.zipcodes.aggregate([ 
    $project : [ city: 1, pop: 1 ] 
  ],
  [ 
    $match : [ pop: [ $gte : 10 * 1000 ] ] 
  ],
  [ 
    $sort: [ pop: -1] 
  ]
)

assert aggrOutput.results().size() == 3
assert aggrOutput.results()[0].city == "NEW SITE"
assert aggrOutput.results()[1].city == "KEYSTONE"
assert aggrOutput.results()[2].city == "ADAMSVILLE"
```

An amazing documentation about Aggregation can be found in the MongoDB
website: http://docs.mongodb.org/manual/applications/aggregation/ .

# Support

Any bug, suggestion or ... whatever.

Blog: http://blog.paulopoiati.com/2010/06/20/gmongo-0-5-released/.

Email: paulogpoiati@gmail.com

Twitter: http://twitter.com/poiati

# Maven

All versions of the project can be found in the maven central repository:

    http://repo1.maven.org/maven2/com/gmongo/gmongo/


# Build

The project is build using gradle. Gradle can be found in: http://www.gradle.org

# Test

To run the tests start a mongo instance on localhost:27017

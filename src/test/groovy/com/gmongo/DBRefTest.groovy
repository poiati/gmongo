package com.gmongo

import com.mongodb.DBRef

class DBRefTest extends IntegrationTestCase {
  
  void setUp() {
    super.setUp()
    db.dropDatabase()
  }
  
  void testDBRefUsage() {
    def author = [_id: 1, name: "Foo"]
    def book = [title: "The foo biography", author: new DBRef(null, 'authors', 1)]

    db.authors << author
    db.books << book
    
    assert db.books.findOne().author.fetch() == author
  }
}
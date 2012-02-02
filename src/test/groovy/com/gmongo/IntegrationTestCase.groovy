package com.gmongo

import com.mongodb.DBAddress
import com.mongodb.WriteConcern

abstract class IntegrationTestCase extends GroovyTestCase {

  static DB_NAME = 'gmongo_test'

  def mongo, db
  int port

  void setUp() {
    port = System.getProperty("MONGO_PORT", "27017") as int
    mongo = new GMongo(new DBAddress('localhost', port, DB_NAME))
    mongo.setWriteConcern(WriteConcern.SAFE)
    db = mongo.getDB(DB_NAME)
  }
}
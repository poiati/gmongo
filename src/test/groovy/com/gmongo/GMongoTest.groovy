package com.gmongo

import com.gmongo.internal.Patcher

import com.mongodb.DB
import com.mongodb.ServerAddress
import com.mongodb.DBAddress

class GMongoTest extends IntegrationTestCase {

  void testGetDbGroovy() {
    assertTrue mongo.getDB(DB_NAME) instanceof DB
    assert mongo.getDB(DB_NAME).hasProperty(Patcher.PATCH_MARK)
  }

  void testGetDb() {
    def db = mongo.getDB(DB_NAME)
    assertTrue db instanceof DB
    assert db.hasProperty(Patcher.PATCH_MARK)
  }

  void testStaticConnect() {
    def db2 = GMongo.connect(new DBAddress('localhost', 27017, DB_NAME))
    assert db2.hasProperty(Patcher.PATCH_MARK)
  }

}
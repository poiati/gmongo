/*
Copyright 2010-2011 Paulo Poiati

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.gmongo

import java.util.List;

import com.mongodb.MongoOptions
import com.mongodb.ServerAddress
import com.mongodb.Mongo
import com.mongodb.DB
import com.mongodb.DBAddress
import com.mongodb.MongoURI

import com.gmongo.internal.DBPatcher

class GMongo {

  @Delegate
  Mongo mongo

  static DB connect(DBAddress addr) {
    patchAndReturn Mongo.connect(addr)
  }

  GMongo(Mongo mongo) {
    this.mongo = mongo
  }

  GMongo() {
    this.mongo = new Mongo()
  }

  GMongo(ServerAddress addr) {
    this.mongo = new Mongo(addr)
  }

  GMongo(ServerAddress addr, MongoOptions opts) {
    this.mongo = new Mongo(addr, opts)
  }

  GMongo(ServerAddress left, ServerAddress right) {
    this.mongo = new Mongo(left, right)
  }

  GMongo(ServerAddress left, ServerAddress right, MongoOptions opts) {
    this.mongo = new Mongo(left, right, opts)
  }

  GMongo(String host) {
    this.mongo = new Mongo(host)
  }

  GMongo(String host, Integer port) {
    this.mongo = new Mongo(host, port)
  }

  GMongo(String host, MongoOptions opts) {
    this.mongo = new Mongo(host, opts)
  }

  GMongo( List<ServerAddress> replicaSetSeeds, MongoOptions opts ) {
    this.mongo = new Mongo(replicaSetSeeds, opts)
  }

  GMongo( List<ServerAddress> replicaSetSeeds) {
    this.mongo = new Mongo(replicaSetSeeds)
  }
  
  GMongo( MongoURI mongoURI ) {
    this.mongo = new Mongo( mongoURI )
  }

  DB getDB(String name) {
    patchAndReturn mongo.getDB(name)
  }

  static private patchAndReturn(db) {
    DBPatcher.patch(db); return db
  }
}
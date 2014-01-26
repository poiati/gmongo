/*
Copyright 2010-2014 Paulo Poiati

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

import com.mongodb.ServerAddress
import com.mongodb.MongoOptions
import com.mongodb.MongoURI
import com.mongodb.MongoClientURI
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.MongoClient
import com.mongodb.DB

import com.gmongo.internal.DBPatcher

class GMongoClient {

  @Delegate
  MongoClient mongoClient;

  GMongoClient() {
    this.mongoClient = new MongoClient(new ServerAddress())
  }

  GMongoClient(String host) {
    this.mongoClient = new MongoClient(new ServerAddress(host))
  }

  GMongoClient(String host, MongoClientOptions options) {
    this.mongoClient = new MongoClient(new ServerAddress(host), options)
  }

  GMongoClient(String host, int port) {
    this.mongoClient = new MongoClient(new ServerAddress(host, port));
  }

  GMongoClient(ServerAddress addr) {
    this.mongoClient = new MongoClient(addr, new MongoClientOptions.Builder().build());
  }

  GMongoClient(ServerAddress addr, MongoClientOptions options) {
    this.mongoClient = new MongoClient(addr, new MongoOptions(options));
  }

  GMongoClient(List<ServerAddress> seeds) {
    this.mongoClient = new MongoClient(seeds, new MongoClientOptions.Builder().build());
  }

  GMongoClient(List<ServerAddress> seeds, List<MongoCredential> credentialsList) {
    this.mongoClient = new MongoClient(seeds, credentialsList);
  }

  GMongoClient(List<ServerAddress> seeds, List<MongoCredential> credentialsList, MongoClientOptions options) {
    this.mongoClient = new MongoClient(seeds, credentialsList, options);
  }

  GMongoClient(List<ServerAddress> seeds, MongoClientOptions options) {
    this.mongoClient = new MongoClient(seeds, new MongoOptions(options));
  }

  GMongoClient(MongoClientURI uri) {
    this.mongoClient = new MongoClient(uri);
  }

  GMongoClient(ServerAddress addr, List<MongoCredential> credentialsList) {
    this.mongoClient = new MongoClient(addr, credentialsList);
  }

  GMongoClient(ServerAddress addr, List<MongoCredential> credentialsList, MongoClientOptions options) {
    this.mongoClient = new MongoClient(addr, credentialsList, options);
  }

  DB getDB(String name) {
    patchAndReturn mongoClient.getDB(name)
  }

  static private patchAndReturn(db) {
    DBPatcher.patch(db); return db
  }

}

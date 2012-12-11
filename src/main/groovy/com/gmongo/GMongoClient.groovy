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

import com.mongodb.ServerAddress
import com.mongodb.MongoOptions
import com.mongodb.MongoURI
import com.mongodb.MongoClientURI
import com.mongodb.MongoClientOptions

class GMongoClient extends GMongo {

  GMongoClient() {
    this(new ServerAddress())
  }

  GMongoClient(String host) {
    this(new ServerAddress(host))
  }

  GMongoClient(String host, MongoClientOptions options) {
    this(new ServerAddress(host), options);
  }

  GMongoClient(String host, int port) {
    this(new ServerAddress(host, port));
  }

  GMongoClient(ServerAddress addr) {
    this(addr, new MongoClientOptions.Builder().build());
  }

  GMongoClient(ServerAddress addr, MongoClientOptions options) {
    super(addr, new MongoOptions(options));
  }

  GMongoClient(List<ServerAddress> seeds) {
    this(seeds, new MongoClientOptions.Builder().build());
  }

  GMongoClient(List<ServerAddress> seeds, MongoClientOptions options) {
    super(seeds, new MongoOptions(options));
  }

  GMongoClient(MongoClientURI uri) {
    super(new MongoURI(uri));
  }

}

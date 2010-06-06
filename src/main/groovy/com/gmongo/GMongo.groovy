/*
Copyright 2010 Paulo Poiati

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

import com.mongodb.Mongo
import com.mongodb.DB
import com.mongodb.DBAddress
import com.gmongo.internal.Patcher

class GMongo {
	
	@Delegate
	Mongo mongo
	
	GMongo() {
		mongo = new Mongo()
	}
	
	GMongo(DBAddress addr) {
		mongo = new Mongo(addr)
	}
	
	DB getDb() {
		def db = mongo.getDB()
		Patcher.patchDb(db)
		return db
	}
}
package com.gmongo

import com.mongodb.DB

class MongoTest extends IntegrationTestCase {
	
	void testGetDb() {
		assertTrue mongo.db instanceof DB
	}
	
}
package com.gmongo

import com.mongodb.DBAddress

class IntegrationTestCase extends GroovyTestCase {
	
	def mongo
	
	void setUp() {
		mongo = new GMongo(new DBAddress('localhost', 27017, 'gmongo_test'))
	}
	
	void testNothing() {}
}
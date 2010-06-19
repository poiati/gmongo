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
package com.gmongo.internal

import com.mongodb.DBObject
import com.mongodb.BasicDBObject

class DBCollectionPatcher {
	
	static final PATCHED_METHODS = [ 
		'insert', 'find', 'findOne','remove', 'save', 'count', 'update', 
		'updateMulti', 'distinct', 'apply', 'createIndex', 'ensureIndex',
		'mapReduce', 'dropIndex', 'getCount', 'group', 'setHintFields'
	]
	
	static final ALIAS = [
		leftShift : 'insert', 
		rightShift: 'remove'
	]
	
	static final ADDITIONAL_METHODS = [
		update: { DBObject q, DBObject o, Boolean upsert ->
			delegate.update(q, o, upsert, false)
		}
	]
	
	static final AFTER_RETURN = [
		apply: { defaultArgs, result ->
			defaultArgs[0]._id = result
		}
	]
	
	static patch(c) {
		if (c.hasProperty(Patcher.PATCH_MARK))
			return
		Patcher._patchInternal c, PATCHED_METHODS, ALIAS, ADDITIONAL_METHODS, AFTER_RETURN
	}
}
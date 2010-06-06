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

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.DBCollection

class Patcher {
	
	static final PATCHED_METHODS = [ 
		'insert', 'find', 'findOne','remove', 'save', 'count', 'update', 
		'updateMulti'
	]
	static final ALIAS = 	[ 
		leftShift : 'insert', 
		rightShift: 'remove'
	]
	static final ADDITIONAL_METHODS = [
		update: { DBObject q, DBObject o, Boolean upsert ->
			delegate.update(q, o, upsert, false)
		}
	]
	
	static patchDb(db) {
		db.metaClass.getProperty = { name ->
			def c = delegate.getCollection(name)
			patchCollection(c)
			return c
		}
	}

	static patchCollection(c) {
		c.metaClass.invokeMethod = { String name, args ->
			def nameOrAlias = ALIAS[name] ?: name
			if (nameOrAlias in PATCHED_METHODS) {
				_convert(args)
				def method = c.class.metaClass.getMetaMethod(nameOrAlias, _types(args, true))
				if (method == null) {
					def other = ADDITIONAL_METHODS[nameOrAlias]
					if (other != null) {
						other.delegate = c
						def largs = []
						for (arg in args) largs << arg
						return other.call(largs) 
					}
				}
				return method.doMethodInvoke(delegate, args)
			}
			def method = c.class.metaClass.getMetaMethod(nameOrAlias, _types(args))
			method.doMethodInvoke(delegate, args)
		}
	}
	
	private static _convert(args) {
		def size = (args instanceof List) ? args.size() : args.length
		for (def i = 0; i < size; i++) {
			if (args[i] instanceof List) {
				_convert(args[i])
				continue
			}
			if ((!(args[i] instanceof Map)) || (args[i] instanceof DBObject)) {
				continue
			}
			args[i] = (args[i] as BasicDBObject)
		}
	}
	
	private static Object[] _types(args, handleDbObjects=false) {
		def types = []
		for (arg in args) {
			if (handleDbObjects) {
				if ((arg instanceof Map) && (!(arg instanceof DBObject))) {
					types << DBObject
					continue
				}
			}
			types << arg.getClass()
		}
		return types as Object[]
	}
}
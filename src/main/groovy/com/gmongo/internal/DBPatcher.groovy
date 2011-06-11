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
package com.gmongo.internal

import static com.gmongo.internal.Patcher.*

import com.mongodb.DB
import com.mongodb.DBObject
import com.mongodb.BasicDBObject

class DBPatcher {
  
  // Methods that return a collection and need special handling
  static final METHODS_RETURNING_COLLECTION = "getCollection getCollectionFromFull getCollectionFromString".split(/\s+/)
  static final METHOD_COMMAND = "command"
  static final METHOD_CREATE_COLLECTION = "createCollection"
  
  static patch( db ) {
    if ( _isPatched( db ) ) return
    
    def _simpleMapToDBObjectPatchDB = _simpleMapToDBObjectPatch.curry( DB )
    
    db.metaClass.with {
      
      // Must add methods to DBCollection MetaClass before returning
      this.METHODS_RETURNING_COLLECTION.each {
        delegate[it] = { String name -> _invokeOriginal delegate, name }
      }
      
      // Property calls must return a collection
      getProperty = { name ->
        _patchedCollection( delegate.getCollection( name ) )
      }
      
      // Execute a code block between DB#requestStart and DB#requestDone
      inRequest = { Closure fn ->
        delegate.requestStart()
        try {
            fn.call()
        } finally {
            delegate.requestDone()
        }
      }
      
      // Add methods that accept a Map in the place of a DBObject
      command = _simpleMapToDBObjectPatchDB.curry( DBPatcher.METHOD_COMMAND )
      createCollection = this._simpleStringMapToDBObjectPatch.curry( DB, DBPatcher.METHOD_CREATE_COLLECTION )
    }
    
    // Mark this instance as Patched
    _markAsPatched( db )
    return db
  }
  
  static _simpleStringMapToDBObjectPatch = { clazz, methodName, String value, Map object ->
    def method = _findMetaMethod( clazz, methodName, [ String, DBObject ] )
    return _patchedCollection( _invokeMethod( method, delegate, [ value, object as BasicDBObject ] ) )
  }
  
  private static _patchedCollection( c ) {
    if (c.hasProperty(Patcher.PATCH_MARK)) return c
    DBCollectionPatcher.patch(c)
    return c
  }

  private static _invokeOriginal( delegate, name ) {
    def original = _findMetaMethod( DB, "getCollection", [ String ] as Object[] )
    return _patchedCollection( original.doMethodInvoke( delegate, name ) )
  }
}
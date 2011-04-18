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

import static com.gmongo.internal.Patcher.*

import com.mongodb.DB
import com.mongodb.BasicDBObject

class DBPatcher {

  static final AFTER_RETURN = [
    createCollection: { defaultArgs, result ->
      DBCollectionPatcher.patch result
    }
  ]

  static patch( db ) {
    if ( _isPatched( db ) ) return
    
    def _simpleMapToDBObjectPatchDB = _simpleMapToDBObjectPatch.curry( DB )
    
    db.metaClass.with {
      getCollection = { String name ->
        _invokeOriginal delegate, "getCollection", name
      }
      getCollectionFromFull = { String name ->
        _invokeOriginal delegate, "getCollection", name
      }
      getCollectionFromString = { String name ->
        _invokeOriginal delegate, "getCollection", name
      }	
      getProperty = { name ->
        return _patchedCollection(delegate.getCollection(name))
      }
      inRequest = { Closure fn ->
        delegate.requestStart()
        try {
            fn.call()
        } finally {
            delegate.requestDone()
        }
      }
      
      
      command = _simpleMapToDBObjectPatchDB.curry( "command" )
      
    }
    
    db.metaClass.createCollection = _simpleStringMapToDBObjectPatch.curry( DB, "createCollection" )
    
    _markAsPatched( db )
    
    return db
  }
  
  static _simpleStringMapToDBObjectPatch = { clazz, methodName, String value, Map object ->
    def method = _findMetaMethod( clazz, methodName, [ String, DBObject ] )
    return _patchedCollection( _invokeMethod( method, delegate, [ value, object as BasicDBObject ] ) )
  }
  
  private static _patchedCollection(c) {
    if (c.hasProperty(Patcher.PATCH_MARK)) return c
    DBCollectionPatcher.patch(c)
    return c
  }

  private static _invokeOriginal(delegate, methodName, name) {
    def original = delegate.class.metaClass.getMetaMethod(methodName, [ String ] as Object[])
    return _patchedCollection(original.doMethodInvoke(delegate, name))
  }
}
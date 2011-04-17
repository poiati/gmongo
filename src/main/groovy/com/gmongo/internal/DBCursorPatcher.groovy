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
import com.mongodb.DBCursor
import com.mongodb.BasicDBObject

class DBCursorPatcher {
  
  static patch( cursor ) {
    if ( cursor.hasProperty( Patcher.PATCH_MARK ) ) return
    
    _patchSort( cursor )
    _patchHint( cursor )
    _patchCopy( cursor )
    
    Patcher._markAsPatched( cursor )
    
    return cursor
  }
  
  private static _patchSort( cursor ) {
    cursor.metaClass.sort = _simpleMapToDBObjectPatch.curry( "sort" )
  }
  
  private static _patchHint( cursor ) {
    cursor.metaClass.hint = _simpleMapToDBObjectPatch.curry( "hint" )
  }
  
  private static _patchCopy( cursor ) {
    cursor.metaClass.copy = { ->
      def method = _findMetaMethod( "copy", [ ] )
      return patch( _invokeMethod( method, delegate, [] as Object[] ) )
    }
  }
  
  private static _simpleMapToDBObjectPatch = { methodName, Map object ->
    def method = _findMetaMethod( methodName, [ DBObject ])
    return _invokeMethod( method, delegate, [ object as BasicDBObject ] as Object[ ] )
  }
  
  private static _findMetaMethod( methodName, args ) {
    return DBCursor.metaClass.getMetaMethod( methodName, args as Object[ ] )
  }
  
  private static _invokeMethod( method, target, args ) {
    return method.invoke( target, args as Object[ ] )
  }
}
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

import com.mongodb.DBCursor

class DBCursorPatcher {
  
  // Methods to be patched
  static final SIMPLE_METHODS = [ "sort", "hint" ]
  static final COPY_METHOD = "copy"
  
  // Patch the cursor instance
  static patch( cursor ) {
    if ( _isPatched( cursor ) ) return
    
    def _simpleMapToDBObjectPatchDBCursor = _simpleMapToDBObjectPatch.curry( DBCursor )
    
    // Create a Map version for each method that accept a single DBObject 
    cursor.metaClass.with {
      DBCursorPatcher.SIMPLE_METHODS.each { delegate[ it ] = _simpleMapToDBObjectPatchDBCursor.curry( it ) }
    }
    
    _patchCopy( cursor )
    _markAsPatched( cursor )
    return cursor
  }
  
  // Patch the copy method to return a patched DBCollection
  private static _patchCopy( cursor ) {
    cursor.metaClass.copy = { ->
      def method = _findMetaMethod( DBCursor, COPY_METHOD, [ ] )
      return patch( _invokeMethod( method, delegate, [ ] ) )
    }
  }
}
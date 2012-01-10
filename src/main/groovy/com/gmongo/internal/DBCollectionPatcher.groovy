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

import com.mongodb.DBObject
import com.mongodb.BasicDBObject

class DBCollectionPatcher {
  
  static final PATCHED_METHODS = [ 
    'insert', 'find', 'findOne', 'findAndModify', 'findAndRemove', 'remove', 'save', 'count', 'update', 
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
  
  private static final COPY_GENERATED_ID = { defaultArgs, invokeArgs, result ->
    MirrorObjectMutation.copyGeneratedId(invokeArgs.first(), defaultArgs.first())
  }

  static final AFTER_RETURN = [
    apply: { defaultArgs, invokeArgs, result ->
      defaultArgs[0]._id = result
    },
    
    find: { defaultArgs, invokeArgs, result ->
      DBCursorPatcher.patch(result)
    },

    save: COPY_GENERATED_ID, insert: COPY_GENERATED_ID
  ]

  static patch(c) {
    if (c.hasProperty(Patcher.PATCH_MARK))
      return
    _addCollectionTruth(c)
    Patcher._patchInternal c, PATCHED_METHODS, ALIAS, ADDITIONAL_METHODS, AFTER_RETURN
  }
  
  private static _addCollectionTruth(c) {
    c.metaClass.asBoolean { -> delegate.count() > 0 }
  }
}

class MirrorObjectMutation {
  
  static void copyGeneratedId(Object[] from, Object[] to) {
    copyGeneratedId(from as List, to as List)
  }
  
  static void copyGeneratedId(List from, List to) {
    from.size().times {
      copyGeneratedId(from[it], to[it])
    }
  }
  
  static void copyGeneratedId(Map from, Map to) {
    to._id = from._id
  }
  
}
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

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.DBCursor

class Patcher {

  static final PATCH_MARK = '__decorated'

  static _patchInternal(target, pmethods, alias=[:], addmethods=[:], afterreturn=[:]) {
    target.metaClass.invokeMethod = { String name, args ->
      def nameOrAlias = alias[name] ?: name
      if (nameOrAlias in pmethods) {
        def cargs = _convert(args)
        def method = target.class.metaClass.getMetaMethod(nameOrAlias, _types(cargs, true))
        if (method == null) {
          def other = _getAdditionalMethod(addmethods, nameOrAlias, target, args)
          return other.call(cargs as List) 
        }
        return _invoke(method, delegate, args, cargs, afterreturn)
      }
      def method = target.metaClass.getMetaMethod(nameOrAlias, _types(args))
      if (!method) 
        throw new MissingMethodException(name, target.class, args)
      return _invoke(method, delegate, args, args, afterreturn)
    }
    _markAsPatched(target)
  }

  static _invoke(method, delegate, originalArgs, invokeArgs, afterreturn) {
    def result = method.doMethodInvoke(delegate, invokeArgs)
    afterreturn.get(method.name)?.call(originalArgs, invokeArgs, result)
    return result
  }
  
  static _getAdditionalMethod(additionalMethods, nameOrAlias, target, args) {
    def other = additionalMethods[nameOrAlias]
    if (other == null)
      throw new MissingMethodException(nameOrAlias, target.class, args)
    other.delegate = target
    return other
  }

  static _convert(args) {
    def size = (args instanceof List) ? args.size() : args.length
    def convertedArgs = []
    for (def i = 0; i < size; i++) {
      if (args[i] instanceof List) {
        convertedArgs[i] = _convert(args[i])
        continue
      }
      if ( args[i] instanceof Map )
        _converAllCharSeqToString(args[i])
      if (!(args[i] instanceof Map) || (args[i] instanceof DBObject)) {
        convertedArgs[i] = args[i]
        continue
      }
      convertedArgs[i] = (args[i] as BasicDBObject)
    }
    return ((args instanceof List) ? convertedArgs : (convertedArgs as Object[]))
  }
  
  static _converAllCharSeqToString(map) {
    map.each { entry ->
      def val = entry.value
      if (val instanceof List) {
        val.eachWithIndex { element, i ->
          if (element instanceof CharSequence) {
            val[i] = element.toString()
            return
          }
          if (element instanceof Map) {
            _converAllCharSeqToString(element)
          }
        }
      }
      if (val instanceof CharSequence) {
        map.put(entry.key, val.toString())
        return
      }
      if (val instanceof Map) {
        _converAllCharSeqToString(val)
      } 
    }
  }

  static Object[] _types(args, handleDbObjects=false) {
    def types = []
    for (arg in args) {
      if (handleDbObjects) {
        if ((arg instanceof Map) && (!(arg instanceof DBObject))) {
          types << DBObject
          continue
        }
      }
      types << arg?.getClass()
    }
    return types as Object[]
  }
  
  static _markAsPatched(target) {
    target.metaClass[Patcher.PATCH_MARK] = true
  }
  
  static _isPatched(target) {
    target.hasProperty(Patcher.PATCH_MARK)
  }
  
  // Return a closure that transform a plain Map in a BasicDBObject
  static _simpleMapToDBObjectPatch = { clazz, methodName, Map object ->
    def method = _findMetaMethod( clazz, methodName, [ DBObject ])
    return _invokeMethod( method, delegate, [ object as BasicDBObject ] as Object[ ] )
  }

  static _findMetaMethod( clazz, methodName, args ) {
    def method = clazz.metaClass.getMetaMethod( methodName, args as Object[ ] )
    return clazz.metaClass.getMetaMethod( methodName, args as Object[ ] )
  }

  static _invokeMethod( method, target, args ) {
    return method.invoke( target, args as Object[ ] )
  }
}
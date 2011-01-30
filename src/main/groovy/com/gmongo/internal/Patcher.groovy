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

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.BasicDBObject
import com.mongodb.DBObject

class Patcher {

  static final PATCH_MARK = '__decorated'

  static _patchInternal(o, pmethods, alias=[:], addmethods=[:], afterreturn=[:]) {
    o.metaClass.invokeMethod = { String name, args ->
      def nameOrAlias = alias[name] ?: name
      if (nameOrAlias in pmethods) {
        def cargs = _convert(args)
        def method = o.class.metaClass.getMetaMethod(nameOrAlias, _types(cargs, true))
        if (method == null) {
          def other = addmethods[nameOrAlias]
          if (other == null)
          throw new MissingMethodException(name, o.class, args)
          other.delegate = o
          def largs = []
          for (arg in cargs) largs << arg
          return other.call(largs) 
        }
        def result = method.doMethodInvoke(delegate, cargs)
        afterreturn.get(nameOrAlias)?.call(args, result)
        return result
      }
      def method = o.metaClass.getMetaMethod(nameOrAlias, _types(args))
      if (!method)
      throw new MissingMethodException(name, o.class, args)
      method.doMethodInvoke(delegate, args)
    }
    o.metaClass[Patcher.PATCH_MARK] = true
  }

  static _convert(args) {
    def size = (args instanceof List) ? args.size() : args.length
    def convertedArgs = []
    for (def i = 0; i < size; i++) {
      if (args[i] instanceof List) {
        convertedArgs[i] = _convert(args[i])
        continue
      }
      if ((!(args[i] instanceof Map)) || (args[i] instanceof DBObject)) {
        convertedArgs[i] = args[i]
        continue
      }
      convertedArgs[i] = (args[i] as BasicDBObject)
    }
    return ((args instanceof List) ? convertedArgs : (convertedArgs as Object[]))
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
      types << arg.getClass()
    }
    return types as Object[]
  }
}
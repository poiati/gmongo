package com.gmongo.internal

class DBCursorPatcher {
  
  static final PATCHED_METHODS = [ 'sort', 'hint' ]
  
  static final AFTER_RETURN = [
    copy: { defaultArgs, result ->
      DBCursorPatcher.patch(result)
    }
  ]
  
  static patch(cursor) {
    if (cursor.hasProperty(Patcher.PATCH_MARK)) return
    Patcher._patchInternal cursor, PATCHED_METHODS, [:], [:], AFTER_RETURN
  }
}
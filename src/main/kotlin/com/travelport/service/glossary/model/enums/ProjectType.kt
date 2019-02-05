package com.travelport.service.glossary.model.enums

enum class ProjectType {
  UNKNOWN,
  SERVICE,
  LIBRARY,
  IOS_APP,
  IOS_LIB,
  ANDROID_APP,
  ANDROID_LIB,
  ;

  companion object {
    @Throws(IllegalArgumentException::class)
    fun fromString(incoming: String?): ProjectType? {
      if (incoming == null) return UNKNOWN
      return try {valueOf(incoming.toUpperCase())} catch (e: IllegalArgumentException) {UNKNOWN}
    }
  }
}

package com.travelport.service.directory.model.enums

enum class ProjectType {
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
      if (incoming == null) return null
      return valueOf(incoming.toUpperCase())
    }
  }
}

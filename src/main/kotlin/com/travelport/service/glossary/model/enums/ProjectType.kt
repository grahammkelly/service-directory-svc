package com.travelport.service.glossary.model.enums

import java.util.*

enum class ProjectType(val matchers: List<List<String>> = emptyList()) {
  SERVICE(listOf(listOf("jvm", "spring-boot"), listOf("nodejs", "express"))),
  LIBRARY(listOf(listOf("jvm", "library"), listOf("nodejs", "library"))),
  IOS_APP(listOf(listOf("ios", "application"))),
  IOS_LIB(listOf(listOf("ios", "library"))),
  ANDROID_APP(listOf(listOf("android", "application"))),
  ANDROID_LIB(listOf(listOf("android", "library"))),
  UNKNOWN,
  ;

  companion object {
    @Throws(IllegalArgumentException::class)
    fun fromString(incoming: String?): ProjectType? {
      if (incoming == null) return UNKNOWN
      return try {valueOf(incoming.toUpperCase())} catch (e: IllegalArgumentException) {UNKNOWN}
    }

    fun matching(platform: String, type: String) =
        values().find { it.matchesFor(platform, type) } ?: UNKNOWN
  }

  private fun matchesFor(platform: String, type: String) =
      matchers.isEmpty() ||
          matchers.find { it[0] ==  platform && it[1] == type } != null
}

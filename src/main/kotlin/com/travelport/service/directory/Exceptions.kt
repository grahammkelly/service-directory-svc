package com.travelport.service.directory

class ProjectBadlyFormatted(message: String): Exception(message)

class NoProjectInfo: Exception()

abstract class UnknownProject(message: String): Exception(message)

class UnknownProjectId(val id: Long): UnknownProject("id ${id}")

class UnknownProjectName(val projectName: String, val version: String? = null):
    UnknownProject("'${projectName}'" + (if (version!=null && version.isNotEmpty()) "/${version}" else "")) {
  override val message get() = super.message
}

package com.travelport.service.directory

class NoProjectInfo(): Exception()

abstract class UnknownProject(message: String): Exception(message)

class UnknownProjectId(val id: Long): UnknownProject("id ${id}")

class UnknownProjectName(val projectName: String, val version: String? = null):
    UnknownProject("'${projectName}'" + (if (version!=null && version.isNotEmpty()) "/${version}" else "")) {
  override val message get() = super.message
}

class NotImplemented(val method: String): Exception("Method '${method}' is not implemented yet") {
  override val message get() = super.message
}

package com.travelport.service.directory.model.exceptions

class NoProjectInfo(override val message: String = "", override val cause: Throwable? = null): Exception(message, cause)

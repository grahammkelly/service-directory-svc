package com.travelport.service.directory.api

import com.travelport.service.directory.NoProjectInfo
import com.travelport.service.directory.NotImplemented
import com.travelport.service.directory.UnknownProject
import org.joda.time.DateTime
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.NOT_IMPLEMENTED
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class ExceptionHandler {
  @ExceptionHandler(UnknownProject::class)
  @ResponseStatus(NOT_FOUND)
  fun handleProjectNotFound(req: HttpServletRequest, e: UnknownProject): ApiError {
    return ApiError("Project ${e.message} does not exist", req.requestURI, NOT_FOUND)
  }

  @ExceptionHandler(NoProjectInfo::class)
  @ResponseStatus(BAD_REQUEST)
  fun handleNoProjectData(req: HttpServletRequest): ApiError  {
    return ApiError("No Project information", req.requestURI, BAD_REQUEST)
  }

  class ApiError(
      val error: String,
      val url: String,
      val status: HttpStatus) {
    val timeStamp = DateTime.now()
  }
}

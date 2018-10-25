package com.travelport.service.glossary.api

import com.travelport.service.glossary.NoProjectInfo
import com.travelport.service.glossary.ProjectBadlyFormatted
import com.travelport.service.glossary.UnknownProject
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class ExceptionHandler {
  private val logger = LoggerFactory.getLogger(ExceptionHandler::class.java)

  @ExceptionHandler(UnknownProject::class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  fun handleProjectNotFound(req: HttpServletRequest, e: UnknownProject): ApiError {
    return ApiError.NotFound("Project ${e.message} does not exist", req.requestURI)
  }

  @ExceptionHandler(NoProjectInfo::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun handleNoProjectData(req: HttpServletRequest): ApiError  {
    return ApiError.BadRequest("No Project information", req.requestURI)
  }

  @ExceptionHandler(ProjectBadlyFormatted::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  fun handleBadlyFormattedIncomingProject(req: HttpServletRequest, e: ProjectBadlyFormatted): ApiError {
    return ApiError.BadRequest(e.message, req.requestURI)
  }

  @ExceptionHandler(Exception::class)
  @ResponseStatus(INTERNAL_SERVER_ERROR)
  @ResponseBody
  fun handleRandomUnknownCrap(req: HttpServletRequest, e: Exception): ApiError {
    logger.warn("An uncaught exception is being handled by the default handler.\nYou should probably handle this explicitly\n\n${e.message}", e)
    return ApiError.InternalError(e.message, req.requestURI)
  }

  class ApiError private constructor(
      val error: String,
      val url: String,
      val status: HttpStatus) {
    val timeStamp = DateTime.now()

    companion object {
      fun BadRequest(error: String?, url: String) = ApiError(error ?: "Bad request", url, HttpStatus.BAD_REQUEST)
      fun NotFound(project: String, url: String) =
          ApiError("Project ${project} does not exist", url, HttpStatus.NOT_FOUND)
      fun InternalError(error: String?, url: String) =
          ApiError(error ?: "Something wonky happened", url, HttpStatus.INTERNAL_SERVER_ERROR)
    }
  }
}

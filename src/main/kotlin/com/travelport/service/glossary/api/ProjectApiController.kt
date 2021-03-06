package com.travelport.service.glossary.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.travelport.service.glossary.NoProjectInfo
import com.travelport.service.glossary.ProjectBadlyFormatted
import com.travelport.service.glossary.UnknownProjectId
import com.travelport.service.glossary.UnknownProjectName
import com.travelport.service.glossary.config.ServiceGlossaryConfiguration
import com.travelport.service.glossary.metrics.MeasuringService
import com.travelport.service.glossary.model.ProjectInfo
import com.travelport.service.glossary.service.ProjectService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.slf4j.event.Level.DEBUG
import org.slf4j.event.Level.INFO
import org.slf4j.event.Level.TRACE
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class ProjectApiController: MeasuringService() {
  val logger: Logger = LoggerFactory.getLogger(ProjectApiController::class.java)

  @Autowired
  lateinit var om: ObjectMapper

  @Autowired
  lateinit var yamlMapper: Yaml

  @Autowired
  lateinit var cfg: ServiceGlossaryConfiguration

  @Autowired lateinit var svc: ProjectService

  @GetMapping(path = ["/project/{arg}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(OK)
  fun getProject(@PathVariable arg: String): ProjectInfo {
    logger.debug("API Request for project {}", arg)

    val project = if (arg.toLongOrNull() != null) getProjectById(arg.toLong()) else getProjectForName(arg)

    logger.info("Request for project '{}' found [{}]", arg, project.importantInfo())
    return logAndReturn("Returning", project)
  }

  private fun getProjectForName(name: String): ProjectInfo {
    setEventName(GET, "/api/project/{name}")
    logger.debug("API Request for project named {}", name)

    return svc.getProjectNamed(name) ?: throw UnknownProjectName(name)
  }

  private fun getProjectById(id: Long): ProjectInfo {
    setEventName(GET, "/api/project/{id}")
    logger.debug("API Request for project ID {}", id)

    return svc.getId(id) ?: throw UnknownProjectId(id)
  }

  @GetMapping(path = ["/projects"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(OK)
  fun listProjects(): Map<String, String> {
    logger.debug("Request to list all projects")

    val projects = svc.list()
    val projLinks = projects.map{
      it.name to
          linkTo(methodOn(ProjectApiController::class.java).getProject(it.name))
              .withSelfRel().href
    }.toMap()

    logger.info("Found ${projLinks.size} projects")
    return logAndReturn("Returning", projLinks)
  }

  @PostMapping(path = ["/project/{repoName}/{version}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(OK)
  fun postProject(@PathVariable repoName: String, @PathVariable version: String,
      @Valid @RequestBody projectStr: String): Map<String, Any> {
    setEventName(POST, "/api/project/{repoName}/{version}")
    logger.debug("POST received for project {}", repoName)

    val project =
        try {
          val incomingYaml = yamlMapper.loadAs(projectStr, Incoming::class.java)
          val projectInfo = incomingYaml?.projectInfo
          projectInfo?.cleanUp(repoName, cfg.git.baseAddr)
              ?: throw NoProjectInfo()
        } catch (e: YAMLException) {
          throw ProjectBadlyFormatted(e.message ?: "Unable to parse incoming data")
        }

    logger.info("Received for storage: {${project.importantInfo()}}")
    logger.debug(om.writeValueAsString(project))
    svc.save(project)

    val returnVal = mapOf(
        Pair("id", project.id),
        Pair("name", project.name),
        Pair("link",
            linkTo(methodOn(ProjectApiController::class.java).getProject(project.name))
                .withSelfRel().href)
    )

    return logAndReturn("Stored, returning", returnVal)
  }

  @PostMapping(path = ["/project/{repoName}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(OK)
  fun postProject(@PathVariable repoName: String, @Valid @RequestBody projectStr: String): Map<String, Any> =
      postProject(repoName, "", projectStr)

  @DeleteMapping(path = ["/project/{arg}"])
  @ResponseStatus(OK)
  fun deleteProject(@PathVariable arg: String) {
    if (arg.toLongOrNull() != null) deleteProjectId(arg.toLong()) else deleteProjectName(arg)
    logger.warn("Project {} deleted", arg)
  }

  private fun deleteProjectId(id: Long) {
    setEventName(DELETE, "/api/project/{id}")
    logger.warn("Delete request for project id {} from the collection", id)
    svc.getId(id)?.let{ deleteProject(it) } ?: throw UnknownProjectId(id)
  }

  private fun deleteProjectName(repoName: String, version: String? = "") {
    setEventName(DELETE, "/api/project/{repoName}")
    logger.warn("Delete request for project named '{}' from the collection", repoName)
    svc.getProjectNamed(repoName)?.let{ deleteProject(it) } ?: throw UnknownProjectName(repoName, version ?: "")
  }

  private fun deleteProject(project: ProjectInfo) {
    logger.warn("Deleting project - {}", project.importantInfo())
    svc.delete(project)
  }
  @DeleteMapping(path=["/projects"])
  @ResponseStatus(OK)
  fun deleteAllProjects() {
    logger.warn("Deleting all projects from the collection")
    svc.deleteAllProjects()
    logger.warn("All projects in collection deleted")
  }

  private fun <T> logAndReturn(action: String, returnObject: T, lvl: Level = DEBUG): T {
    fun message() = "${action}: ${om.writeValueAsString(returnObject)}"
    when {
      (lvl == INFO && logger.isInfoEnabled) -> logger.info(message())
      (lvl == DEBUG && logger.isDebugEnabled) -> logger.debug(message())
      (lvl == TRACE && logger.isTraceEnabled) -> logger.trace(message())
    }
    return returnObject
  }

  class Incoming(
      var projectInfo: ProjectInfo? = null
  )
}


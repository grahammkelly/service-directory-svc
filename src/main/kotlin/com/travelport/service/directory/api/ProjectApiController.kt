package com.travelport.service.directory.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.travelport.service.directory.NoProjectInfo
import com.travelport.service.directory.NotImplemented
import com.travelport.service.directory.UnknownProjectId
import com.travelport.service.directory.UnknownProjectName
import com.travelport.service.directory.config.ServiceDirectoryConfiguration
import com.travelport.service.directory.metrics.MeasuringService
import com.travelport.service.directory.model.ProjectInfo
import com.travelport.service.directory.service.ProjectService
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
import org.springframework.http.HttpStatus.CREATED
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
  lateinit var cfg: ServiceDirectoryConfiguration

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
       it.name to linkTo(methodOn(ProjectApiController::class.java).getProjectById(it.id)).withSelfRel().href!!
    }.toMap()

    logger.info("Found ${projLinks.size} projects")
    return logAndReturn("Returning", projLinks)
  }

  @PostMapping(path = ["/project/{repoName}/{version}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(CREATED)
  fun postProject(@PathVariable repoName: String, @PathVariable version: String,
      @Valid @RequestBody projectStr: String): Map<String, String> {
    setEventName(POST, "/api/project/{repoName}/{version}")
    logger.debug("POST received for project {}", repoName)

    val project =
        yamlMapper.loadAs(projectStr, Incoming::class.java)?.projectInfo?.cleanUp(repoName, cfg.git.baseAddr)
            ?: throw NoProjectInfo()

    logger.info("Received for storage: {${project.importantInfo()}}")
    logger.debug(om.writeValueAsString(project))
    svc.save(project)

    val returnVal = mapOf(
        Pair("id", project.id.toString()),
        Pair("name", project.name),
        Pair("link", linkTo(methodOn(ProjectApiController::class.java).getProjectById(project.id)).withSelfRel().href)
    )

    return logAndReturn("Stored", returnVal)
  }

  @PostMapping(path = ["/project/{repoName}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(CREATED)
  fun postProject(@PathVariable repoName: String, @Valid @RequestBody projectStr: String): Map<String, String> =
      postProject(repoName, "", projectStr)

  @DeleteMapping(path = ["/project/{id}"])
  @ResponseStatus(OK)
  fun deleteProjectId(@PathVariable id: Long) {
    setEventName(DELETE, "/api/project/{id}")
    logger.warn("Deleting request for project id {} from the collection", id)
    svc.deleteProjectId(id)
    logger.warn("Project {} deleted", id)
  }


  @DeleteMapping(path = ["/project/{repoName}/{version}"])
  @ResponseStatus(OK)
  fun deleteProjectWithVersion(@PathVariable repoName: String, @PathVariable version: String?) {
    setEventName(DELETE, "/api/project/{repoName}/{version}")
    logger.warn("Delete request for project {}", repoName)

    val project = svc.getProjectNamed(repoName) ?: throw UnknownProjectName(repoName, version)
    logger.warn("Deleting project - ${project.importantInfo()}")
    //    svc.delete(project)
    //    logger.warn("Project {} deleted", repoName)
    throw NotImplemented("delete")
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


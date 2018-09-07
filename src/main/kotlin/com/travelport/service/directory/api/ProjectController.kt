package com.travelport.service.directory.api

import com.mttnow.jsonobjectmapper.ApiObjectMapper
import com.travelport.service.directory.model.ProjectInfo
import com.travelport.service.directory.model.exceptions.NoProjectInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.CREATED
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
class ProjectController {
  val logger: Logger = LoggerFactory.getLogger(ProjectController::class.java)

  @Autowired
  lateinit var om: ApiObjectMapper

  @Autowired
  lateinit var yamlMapper: Yaml

  @PostMapping(path = ["/project/{repoName}/{version}"], produces = ["application/json"])
  @ResponseStatus(CREATED)
  fun postProject(@PathVariable repoName: String, @PathVariable version: String,
      @Valid @RequestBody projectStr: String): ProjectInfo {
    val project = yamlMapper.loadAs(projectStr, Incoming::class.java)?.projectInfo ?:
        throw NoProjectInfo("No project information in incoming ")

    project.cleanUp(repoName)

    logger.info("Received '${project.displayName}' - ${om.writeValueAsString(project)}")
    return project
  }


  class Incoming(
      var projectInfo: ProjectInfo = ProjectInfo()
  )
}


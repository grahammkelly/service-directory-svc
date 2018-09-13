package com.travelport.service.directory.view

import com.fasterxml.jackson.databind.ObjectMapper
import com.travelport.service.directory.api.ProjectApiController
import com.travelport.service.directory.metrics.MeasuringService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ProjectViewController: MeasuringService() {
  @Autowired private lateinit var api: ProjectApiController
  @Autowired private lateinit var om: ObjectMapper

  @GetMapping(path = ["/"])
  fun home(): String {
    return "index"
  }

  @GetMapping(path = ["/project/{projectName}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getProject(@PathVariable projectName: String): String {
    setEventName(GET, "/project/{projectName}")
    val project = api.getProject(projectName)
    return om.writeValueAsString(project)

  }
}

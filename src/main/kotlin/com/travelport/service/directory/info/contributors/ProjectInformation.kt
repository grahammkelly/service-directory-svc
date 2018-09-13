package com.travelport.service.directory.info.contributors

import com.travelport.service.directory.service.ProjectService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component

@Component
class ProjectInformation: InfoContributor  {
  @Autowired private lateinit var projectSvc: ProjectService

  override fun contribute(builder: Info.Builder) {
    builder.withDetail("projects", projectSvc.serviceDetails)
  }
}

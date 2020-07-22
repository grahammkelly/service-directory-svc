package com.travelport.service.glossary.view

import com.travelport.service.glossary.config.ServiceGlossaryConfiguration
import com.travelport.service.glossary.service.SonarQueryService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ProjectViewController {
  private val log = LoggerFactory.getLogger(ProjectViewController::class.java)

  @Autowired private lateinit var cfg: ServiceGlossaryConfiguration
//  @Autowired private lateinit var sonarSvc: SonarQueryService

  @GetMapping(value = ["/", "/index"], produces = [MediaType.TEXT_HTML_VALUE])
  fun index(model: Model): String {
    model.addAllAttributes(mapOf(
        "pageTitle" to  cfg.serviceName
        , "jenkinsAddress" to cfg.jenkinsAddress
        , "gitHost" to cfg.git.baseAddr.replace("https?://".toRegex(), "").replace("/$".toRegex(), "")
        , "sonarHost" to cfg.sonar.baseAddr
    ))
    return "index.html"
  }
}

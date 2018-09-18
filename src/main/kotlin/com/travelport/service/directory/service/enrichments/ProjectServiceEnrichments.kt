package com.travelport.service.directory.service.enrichments

import com.travelport.service.directory.api.ProjectApiController
import com.travelport.service.directory.model.LinkInfo
import com.travelport.service.directory.model.ProjectInfo
import com.travelport.service.directory.repository.ProjectInfoRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.stereotype.Component

@Component
class EnsureProjectReferencesItself: SaveEnrichment<ProjectInfo> {
  val logger = LoggerFactory.getLogger(EnsureProjectReferencesItself::class.java)

  override fun enrich(project: ProjectInfo): ProjectInfo {
    if (project.tags.isEmpty() || !(project.tags.contains(project.name))) {
      val tags = project.tags.toMutableSet()
      tags.apply {
        add(project.name!!)   //Can't get this far without a name!
        project.tags = this.toSet()
        logger.trace("Recording '${project.name}' as a tag of itself")
      }
      project.tags = tags.toSet()
    }
    return project
  }
}

internal fun linkToViewForProject(project: String, logger: Logger? = null):String {
  (logger ?: LoggerFactory.getLogger("com.travelport.service.directory.service.enrichments.ProjectServiceEnrichments"))
      .trace("Creating link for {}", project)
  val targetController = ProjectApiController::class.java
  val a =
      ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(targetController).getProject(project))
          .withSelfRel().href
  return a!!
}

internal fun createLink(project: String, logger: Logger? = null): LinkInfo {
  val link = linkToViewForProject(project, logger)
  return LinkInfo(project, link)
}

@Component
class AddDependenciesOnCurrentProject: ReadEnrichment<ProjectInfo> {
  @Autowired private lateinit var projectStore: ProjectInfoRepository

  val logger = LoggerFactory.getLogger(AddDependenciesOnCurrentProject::class.java)

  override fun enrich(project: ProjectInfo): ProjectInfo {
    //Look for dependencies onto this project
    try {
      //Make the best attempt to enrich, but return the project either way!

      val projects = projectStore.findDependenciesOn(project.name)
      logger.trace("Found {} projects that rely upon '{}'", projects.size, project.name)

      project.related.dependencyOf = projects.map {createLink(it.name)}.toSet()
    } catch (ignored: Throwable) {
      logger.error("Exception caught when enriching '{}' - {}. Ignoring and carrying on", project.name, ignored.message)
    }

    return project
  }
}

@Component
class AddLinksToDependenciesOfCurrentProject: ReadEnrichment<ProjectInfo> {
  override fun enrich(project: ProjectInfo): ProjectInfo {
    project.related.dependsOn?.map { createLink(it) }
        ?.toSet()?.let {
            project.related.apply {
              dependsUpon = it
              dependsOn = null
            }
          }
    return project
  }
}

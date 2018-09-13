package com.travelport.service.directory.service

import com.travelport.service.directory.config.ServiceDirectoryConfiguration
import com.travelport.service.directory.model.ProjectInfo
import com.travelport.service.directory.repository.ProjectInfoRepository
import com.travelport.service.directory.service.enrichments.EnrichingService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
class ProjectService: EnrichingService<ProjectInfo>() {
  private val logger: Logger = LoggerFactory.getLogger(ProjectService::class.java)

  @Autowired private lateinit var cfg: ServiceDirectoryConfiguration

  @Autowired private lateinit var projectStore: ProjectInfoRepository

  @Autowired private lateinit var mongoTmpl: MongoTemplate

  fun list() = projectStore.findAll(Sort.by("name")).filterNotNull()

  fun save(project: ProjectInfo): ProjectInfo {
    logger.debug("Storing project: {${project.importantInfo()}}")
    if (project.id < 1) {
      //First check if this is an update by searching for the name, if so use the found ID. If not, find the next ID to allocate
      //Name should never be null here, as it's validated on entry
      project.id = projectStore.findByName(project.name!!)?.id ?: nextValidProjectId
      logger.trace("Resetting ID for project to ${project.id}")
    }
    return projectStore.save(applySaveEnrichments(project))
  }

  fun getProjectNamed(name: String) = projectStore.findByName(name)?.let { applyReadEnrichments(it) }

  fun getId(id: Long) = projectStore.findById(id).orElse(null)?.let { applyReadEnrichments(it) }

  @Throws(IllegalArgumentException::class)
  fun deleteProjectId(id: Long) {
    if (id < 1) throw IllegalArgumentException("No project to be deleted")
    projectStore.deleteById(id)
  }

  @Throws(IllegalArgumentException::class)
  fun deleteProjectNamed(projectName: String) = delete(getProjectNamed(projectName))

  @Throws(IllegalArgumentException::class)
  fun delete(project: ProjectInfo?) {
    if (project == null) throw IllegalArgumentException("No project to be deleted")
    projectStore.delete(project)
  }

  fun deleteAllProjects() = projectStore.deleteAll()

  val serviceDetails: Map<String, Any>
    get() {
      val projectCount = projectStore.count()
      val details: MutableMap<String, Any> = mutableMapOf()
      details.put("count", projectCount)
      if (projectCount > 0L) {
        details.put("maxProjectId", nextValidProjectId)
      }
      return details
    }

  private val nextValidProjectId: Long
    get() {
      val queryForMaxId = Query().with(Sort(Sort.Direction.DESC, "id")).limit(1)

      val projectWithMaxId = mongoTmpl.findOne(queryForMaxId, ProjectInfo::class.java)
      return if (projectWithMaxId == null || projectWithMaxId.id < cfg.minimumAllocatableId)
        cfg.minimumAllocatableId else projectWithMaxId.id+1L
    }
}

package com.travelport.service.directory.service

import com.travelport.service.directory.config.ServiceDirectoryConfiguration
import com.travelport.service.directory.model.Tag
import com.travelport.service.directory.repository.ProjectInfoRepository
import com.travelport.service.directory.repository.TagRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class TagService {
  private val logger = LoggerFactory.getLogger(TagService::class.java)

  @Autowired private lateinit var tagRepo: TagRepository
  @Autowired private lateinit var projectRepo: ProjectInfoRepository

  fun list() = tagRepo.findAll(Sort.by("name")).filterNotNull()

  fun save(tag: Tag) =
    if (!tag.projects.isEmpty()) tagRepo.save(tag)
    else tryDelete(findTag(tag.name))

  private fun tryDelete(tag: Tag?) =
    if (tag == null) null
    else {tagRepo.delete(tag); null}

  fun findTag(tag: String): Tag? = tagRepo.findById(tag).orElse(null)

  fun clear() {
    if (projectRepo.count() > 0L) throw IllegalStateException("Unable to clear the Tags repository while there are projects stored")
    logger.warn("Deleting all tags")
    tagRepo.deleteAll()
  }

  val serviceDetails: Map<String, Any>
    get() = mutableMapOf("count" to tagRepo.count())
}

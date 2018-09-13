package com.travelport.service.directory.repository

import com.travelport.service.directory.model.ProjectInfo
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface ProjectInfoRepository : MongoRepository<ProjectInfo, Long> {
  fun findByName(projectName: String): ProjectInfo?

  @Query(value ="{ \"related.dependsOn\": ?0 } }", fields = "{name: ?0}")
  fun findDependenciesOn(name: String): List<ProjectRef>

  @Query(value = "{ \"related.dependsOn\": ?0 } }")
  fun findDependenciesUpon(name: String): List<ProjectInfo>

}

data class ProjectRef(var name: String = "")

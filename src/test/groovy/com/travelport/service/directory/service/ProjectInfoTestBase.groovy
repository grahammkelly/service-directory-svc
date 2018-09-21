package com.travelport.service.directory.service

import com.travelport.service.directory.model.ProjectInfo
import com.travelport.service.directory.model.RelatedLinks
import spock.lang.Specification

abstract class ProjectInfoTestBase extends Specification {
  protected final String testProjectName = "test-project"
  protected final Set<String> testUpstreamProjectNames = ["upstream-01", "upstream-02"]
  protected final Set<String> testDownstreamProjectNames = ["downstream-01", "downstream-02"]

  protected ProjectInfo testProject

  def setup() {
    testProject = createProject()
    assert testProject.tags.isEmpty()
    assert testProject.related.dependencyOf.isEmpty()
    assert testProject.related.dependsUpon.isEmpty()
  }

  protected ProjectInfo createProject(long newId = -1L, String newName = testProjectName, Set<String> basedOn = testDownstreamProjectNames) {
    new ProjectInfo().with {
      id = newId
      name = newName
      related = new RelatedLinks().with {
        dependsOn = basedOn
        it
      }
      it
    }
  }
}


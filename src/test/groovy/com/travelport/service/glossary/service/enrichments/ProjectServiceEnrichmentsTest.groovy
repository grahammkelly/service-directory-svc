package com.travelport.service.glossary.service.enrichments

import com.travelport.service.glossary.repository.ProjectInfoRepository
import com.travelport.service.glossary.repository.ProjectRef
import com.travelport.service.glossary.service.ProjectInfoTestBase

class SaveEnrichmentsTest extends ProjectInfoTestBase {
  def "Make sure project references to self are added"() {
    final def cut = new EnsureProjectReferencesItself()

    when:
    cut.enrich(testProject)

    then:
    !testProject.tags.isEmpty()
    testProject.tags.contains(testProjectName)
  }

  def "Make sure project reference to the service code is added if tags exist but the code is not a tag"() {
    final String code = "new"
    final def cut = new EnsureProjectReferencesItsServiceCode()

    testProject.addTag("random-tag")
    testProject.serviceCode = code
    assert !testProject.tags.contains(code)

    when:
    cut.enrich(testProject)

    then:
    testProject.tags.contains(code)
  }

  def "Make sure project reference to the service code is added tags are empty"() {
    final String code = "new"
    final def cut = new EnsureProjectReferencesItsServiceCode()

    testProject.tags = []
    testProject.serviceCode = code
    assert !testProject.tags.contains(code)

    when:
    cut.enrich(testProject)

    then:
    ! testProject.tags.isEmpty()
    testProject.tags.contains(code)
  }
}

class ReadEnrichmentsTest extends ProjectInfoTestBase {
  def "Make sure dependencies on the current project are added"() {
    final def cut = new AddDependenciesOnCurrentProject()
    final def testUpstreamProjects = testUpstreamProjectNames.collect {new ProjectRef(it)}

    given: "the repo will return a controlled list of upstream projects on request"
    final ProjectInfoRepository mockRepo = Mock()
    mockRepo.findDependenciesOn(testProjectName) >> testUpstreamProjects
    cut.projectStore = mockRepo

    when: "the project is enriched"
    final def result = cut.enrich(testProject)

    then: "Upstream dependencies should have been set"
    result.related.dependencyOf != null
    !result.related.dependencyOf.isEmpty()

    and: "the upstream projects are the ones returned by the repo"
    final def upstreamProjectNames = result.related.dependencyOf.collect {it.name}
    upstreamProjectNames.containsAll(testUpstreamProjectNames)

    and: "the generated lisnk are set correctly too"
    final def links = result.related.dependencyOf.collect {it.link}
    linksCorrectForProjects(links, testUpstreamProjectNames)
  }

  def "When any exceptions thrown on retrieval of the upstream projects for dependency addition don't impact further processing"() {
    final def cut = new AddDependenciesOnCurrentProject()
    final def originalProject = testProject.javaCopy()

    given: "the repo will return a controlled list of upstream projects on request"
    final ProjectInfoRepository mockRepo = Mock()
    mockRepo.findDependenciesOn(testProjectName) >> { throw new IllegalStateException("Exception for test purposes") }

    cut.projectStore = mockRepo

    when:
    def result = cut.enrich(testProject)

    then:
    result == originalProject
  }

  def "Make sure any projects this project relies on are linked to"() {
    final def cut = new AddLinksToDependenciesOfCurrentProject()

    when: "we apply the downstream project link enrichment"
    final def result = cut.enrich(testProject)

    then: "the downstream project (names) list should be unset"
    result.related.dependsOn == null || result.related.dependsOn.isEmpty()

    and: "the names and links should be set"
    !result.related.dependsUpon.isEmpty()

    final def names = result.related.dependsUpon.collect {it.name}
    names.containsAll(testDownstreamProjectNames)

    final def links = result.related.dependsUpon.collect {it.link}
    linksCorrectForProjects(links, testDownstreamProjectNames)
  }

  private boolean linksCorrectForProjects(List<String> links, Set<String> projects) {
    !links.find {! projects.contains(it.tokenize('/').last())}
  }
}

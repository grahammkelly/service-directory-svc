package com.travelport.service.directory.api

import java.util.concurrent.TimeUnit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

import com.fasterxml.jackson.databind.ObjectMapper
import com.mttnow.platform.spring.boot.test.auto.configure.SpringBootIntegrationTest
import com.mttnow.platform.spring.boot.test.auto.configure.mongo.NeedsCleanUp
import com.mttnow.platform.spring.boot.test.auto.configure.mongo.NeedsTestData
import com.travelport.service.directory.model.ProjectInfo
import com.travelport.service.directory.repository.ProjectInfoRepository
import org.yaml.snakeyaml.Yaml
import spock.lang.Shared
import spock.lang.Specification

import static com.jayway.awaitility.Awaitility.await
import static org.hamcrest.Matchers.is

@SpringBootIntegrationTest
class ProjectApiControllerIT extends Specification {
  private final String testProjectName = 'test-project'

  @Autowired TestRestTemplate testRestTemplate

  @Autowired ProjectInfoRepository projectRepo

  @Autowired ObjectMapper om
  @Autowired Yaml yamlMapper

  @Shared String projectYaml
  @Shared ProjectInfo expectedProjectInfo

  def setup() {
    projectYaml = new File(getClass().getResource('/data/testProjectAllCorrect.yml').toURI()).text
    expectedProjectInfo =
        (yamlMapper.loadAs(projectYaml, ProjectApiController.Incoming) as ProjectApiController.Incoming).projectInfo  }

  @NeedsCleanUp
  def "POST should store a new Project"() {
    given:
    def originalCount = projectRepo.count()

    when:
    def response = testRestTemplate.postForEntity("/api/project/${testProjectName}", projectYaml, Map)

    await().atMost(2, TimeUnit.SECONDS).until({projectRepo.count()}, is(originalCount + 1))

    then:
    response.statusCode == HttpStatus.CREATED
    response.body.containsKey "id"

    def project = projectRepo.findById(response.body.id).get()

    project.name == testProjectName
    project.type.equalsIgnoreCase(expectedProjectInfo.type)
    project.owner == expectedProjectInfo.owner
  }

  @NeedsTestData("data/example-projects.json")
  def "GET project list should return all projects"() {
    when:
    def response = testRestTemplate.getForEntity("/api/projects", Map)

    then:
    response.statusCode == HttpStatus.OK

    Map<String, String> projects = response.body

    projects.size() == 2

    projects.keySet() == ["test-project-01", "upstream-project-01"].toSet()
  }

  @NeedsTestData("data/example-projects.json")
  def "GET for a project should calculate internal links correctly"() {
    String expectedName = "test-project-01"
    String expectedUpstream = "upstream-project-01"

    when:
    def response = testRestTemplate.getForEntity("/api/project/1", ProjectInfo)

    then: "make sure we receive the correct status"
    response.statusCode == HttpStatus.OK

    and: "the received project is the one we expected"
    response.body.name == expectedName


    and: "the projects depending on this one are calculated correctly"
    def upstream = response.body.related.dependencyOf.asList()
    upstream.size() == 1
    upstream.first().name == expectedUpstream
    upstream.first().link.endsWith("api/project/${expectedUpstream}")
  }

  @NeedsTestData("data/example-projects.json")
  def "DELETE should remove project"() {
    given:
    def originalCount = projectRepo.count()

    when:
    testRestTemplate.delete("/api/project/2")

    then: "Make sure only the 'test-project-01' is left"
    def listResp = testRestTemplate.getForEntity("/api/projects", Map)
    listResp.body.size() == originalCount-1
    listResp.body.containsKey "test-project-01"

    and: "that the remaining project is no longer a dependency of the deleted one"
    def getResp = testRestTemplate.getForEntity("/api/project/test-project-01", ProjectInfo)
    getResp.body.related.dependencyOf.size() == 0
  }
}

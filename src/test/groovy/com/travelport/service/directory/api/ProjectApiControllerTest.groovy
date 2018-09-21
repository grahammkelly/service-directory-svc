package com.travelport.service.directory.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.travelport.service.directory.NoProjectInfo
import com.travelport.service.directory.UnknownProjectId
import com.travelport.service.directory.UnknownProjectName
import com.travelport.service.directory.config.GitInfo
import com.travelport.service.directory.config.ServiceDirectoryConfiguration
import com.travelport.service.directory.model.ProjectInfo
import com.travelport.service.directory.model.enums.ProjectType
import com.travelport.service.directory.service.ProjectService
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.representer.Representer
import spock.lang.Specification

class ProjectApiControllerTest extends Specification {

  ObjectMapper mockJsonMaper = Mock()
  Yaml mockYamlMapper =  Mock()
  ServiceDirectoryConfiguration mockCfg = Mock()
  ProjectService mockProjectSvc = Mock()

  Yaml realMapper = createYamlMapper()

  ProjectApiController cut

  def setup() {
    cut = createClassUnderTest()
  }

  static private def createYamlMapper() {
    Representer representer = new Representer()
    representer.propertyUtils.skipMissingProperties = true
    new Yaml(representer)
  }

  private ProjectApiController createClassUnderTest() {
    new ProjectApiController().with {
      yamlMapper = mockYamlMapper
      cfg = mockCfg
      svc = mockProjectSvc
      om = mockJsonMaper
      return it
    }
  }

  def "listing the projects"() {
    ProjectInfo proj1 = new ProjectInfo(name: 'test-proj1')
    ProjectInfo proj2= new ProjectInfo(name: 'test-proj2')

    when:
    def result = cut.listProjects()

    then:
    1 * mockProjectSvc.list() >> [proj1, proj2]
    result.keySet().asList() == [proj1.name, proj2.name]
    //Not checking the links, as the methods to generate the links are not mockable and I've no idea what links would be generated outside a running Spring app
  }

  def "GET project sending an Id should identify it's an ID and search for the ID"() {
    Long testId = 999L
    ProjectInfo expectedResult = new ProjectInfo(testId, "test-project")

    when:
    def result = cut.getProject(testId.toString())

    then:
    1 * mockProjectSvc.getId(testId) >> expectedResult

    and:
    result == expectedResult
  }

  def "GET project where ID does not exists"() {
    Long testId = 999L

    when:
    cut.getProject(testId.toString())

    then:
    1 * mockProjectSvc.getId(testId) >> null

    and:
    thrown UnknownProjectId
  }

  def "GET project sending a name should identify it's not aan ID and serach for the name"() {
    String testProjectName = 'test-project'
    ProjectInfo expectedResult = new ProjectInfo(999L, testProjectName)

    when:
    def result = cut.getProject(testProjectName)

    then:
    1 * mockProjectSvc.getProjectNamed(testProjectName) >> expectedResult

    and:
    result == expectedResult
  }

  def "GET project where name does not exists"() {
    String testProjectName = 'test-project'

    when:
    cut.getProject(testProjectName)

    then:
    1 * mockProjectSvc.getProjectNamed(testProjectName) >> null

    and:
    thrown UnknownProjectName
  }

  def "POST controller should parse the data body into model object and clean it up"() {
    String testProjectName = "test-project-all-correct"
    Long testId = 999L

    given: "a YAML representation of an object"
    String yamlDataBody = new File(getClass().getResource('/data/testProjectAllCorrect.yml').toURI()).text
    ProjectApiController.Incoming mappedDataBody = realMapper.loadAs(yamlDataBody, ProjectApiController.Incoming)

    when: "The data is passed to the post method"
    def result = cut.postProject(testProjectName, "", yamlDataBody)

    then: "The service should convert the incoming data into the data model"
    1 * mockYamlMapper.loadAs(yamlDataBody, ProjectApiController.Incoming) >> mappedDataBody
    1 * mockCfg.getGit() >> new GitInfo()

    and: "The 'cleaned' data should be saved"
    1 * mockProjectSvc.save(_) >> {ProjectInfo proj ->
      assert proj.name == testProjectName
      assert proj.type == ProjectType.SERVICE.toString()

      proj.id = testId
      return proj
    }

    then: "Expect that the returned data gives the correct information"
    result.id == testId
    result.name == testProjectName
    result.link != null
    result.link != ""
  }

  def "POST with data not containing project information should throw an exception"() {
    String testProjectName = "test-project-all-correct"

    given: "a YAML representation of an pipeline config without project information"
    String pipelineYaml = """
build:
  pipeline: Jenkinsfile
"""

    when: "The data is passed to the post method"
    cut.postProject(testProjectName, "", pipelineYaml)

    then:
    thrown(NoProjectInfo)
  }

  def "DELETE for name should try to verify the project exists before deleting"() {
    def testProjectName = "test-project-for-delete"

    when: "We try to delete a random project"
    cut.deleteProject(testProjectName)

    then : "Service should check the project exists and fail if it does not"
    1 * mockProjectSvc.getProjectNamed(testProjectName) >> null
    thrown UnknownProjectName
  }

  def "DELETE for name should try to remove the document if the project exists"() {
    def testProjectName = "test-project-for-delete"
    def testProject = new ProjectInfo()

    when: "We try to delete a project"
    cut.deleteProject(testProjectName)

    then : "Service should check the project exists and fail if it does not"
    1 * mockProjectSvc.getProjectNamed(testProjectName) >> testProject
    1 * mockProjectSvc.delete(testProject)
  }

  def "DELETE for an ID should not verify the ID, just delete"() {
    Long testId = 999L
    def testProject = new ProjectInfo()

    when: "We try to delete the ID"
    cut.deleteProject(testId.toString())

    then: "The controller verifies the project exists before deleting"
    1 * mockProjectSvc.getId(testId) >> testProject
    1 * mockProjectSvc.delete(testProject)
  }

  def "DELETE on all collectoins should make a collection delete call"() {
    when: "We try to delete everything"
    cut.deleteAllProjects()

    then: "The controller only makes  a single call"
    1 * mockProjectSvc.deleteAllProjects()
  }

}

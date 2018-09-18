package com.travelport.service.directory.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

import com.fasterxml.jackson.databind.ObjectMapper
import com.travelport.service.directory.AppConfig
import com.travelport.service.directory.config.GitInfo
import com.travelport.service.directory.config.ServiceDirectoryConfiguration
import com.travelport.service.directory.model.ProjectInfo
import com.travelport.service.directory.service.ProjectService
import org.spockframework.spring.SpringBean
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ProjectApiController.class)
class ProjectApiControllerWebMvcTest extends Specification {
  private final String baseRepoAddr = "http://example.com/"

  private final Long testId = 999L
  private final String testProjectName = "test-project"

  @TestConfiguration
  static class MvcTestConfiguration {
    private final def appConfig = new AppConfig()

    @Bean
    ObjectMapper createJsonObjectMapper() {
      appConfig.createJsonObjectMapper()
    }

    @Bean
    Yaml createYamlObjectMapper() {
      appConfig.createYamlObjectMapper()
    }
  }

  @Autowired MockMvc mockMvc
  @Autowired ObjectMapper om
  @Autowired Yaml yamlMapper

  @SpringBean ProjectService svc = Mock()
  @SpringBean ServiceDirectoryConfiguration cfg = Mock()

  String projectYaml = new File(getClass().getResource('/data/testProjectAllCorrect.yml').toURI()).text
  ProjectInfo expectedProjectInfo

  def setup() {
    expectedProjectInfo =
        (yamlMapper.loadAs(projectYaml, ProjectApiController.Incoming) as ProjectApiController.Incoming).projectInfo
  }

  def "POST of a valid project should store the project"() {
    given:
    cfg.getGit() >> new GitInfo(baseRepoAddr)

    when: "we POST project details for a test project"
    def result = mockMvc.perform(
        post("/api/project/${testProjectName}")
            .contentType("application/yaml").content(projectYaml)
            .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isCreated()).andReturn()
    Map<String, Object> respBody = om.readValue(result?.mockResponse?.content?.buf, Map)

    then: "expect that object is stored, and it's the object we're expecting"
    1 * svc.save(_) >> {ProjectInfo objToSave ->
      //Cannot verify they are the same, as the contents are modified by the save operation. Verifying key fields that don't change instead
      assert objToSave.displayName == expectedProjectInfo.displayName
      assert objToSave.type.equalsIgnoreCase(expectedProjectInfo.type)
      assert objToSave.owner == expectedProjectInfo.owner

      //Verify the main field that changes
      assert objToSave.name == testProjectName

      //Not pretend we saved it (by setting the ID)
      objToSave.id = testId
      return objToSave
    }

    and: "return values are as expected"
    respBody.containsKey "id"
    respBody.id == testId
    respBody.link.endsWith("/api/project/${testProjectName}")

  }

  def "GET should return a project if it exists"() {
    when:
    def result = mockMvc.perform(get("/api/project/${testId}")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andReturn()
    def resp = om.readValue(result?.mockResponse?.content?.buf, ProjectInfo)

    then: "we should expect a call to retrieve a project for the id"
    svc.getId(testId) >> expectedProjectInfo

    and: "the correct project should be returned"
    resp != null
    resp == expectedProjectInfo

  }

  def "GET Should return NOT_FOUND if project ID does not exist"() {
    when:
    def result = mockMvc.perform(get("/api/project/${testId}")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andReturn()

    def resp = result.modelAndView.model

    then:
    1 * svc.getId(testId) >> null

    and:
    result.response.status == HttpStatus.NOT_FOUND.value()
    resp.containsKey "apiError"
    resp.apiError.status == HttpStatus.NOT_FOUND
    resp.apiError.url.endsWith("/api/project/${testId}")
  }

  def "GET Should return NOT_FOUND if project name does not exist"() {
    when:
    def result = mockMvc.perform(get("/api/project/${testProjectName}")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andReturn()

    def resp = result.modelAndView.model

    then:
    1 * svc.getProjectNamed(testProjectName) >> null

    and:
    result.response.status == HttpStatus.NOT_FOUND.value()
    resp.containsKey "apiError"
    resp.apiError.status == HttpStatus.NOT_FOUND
    resp.apiError.url.endsWith("/api/project/${testProjectName}")
  }

  def "DELETE for a non existent project id should return NOT_FOUND"() {
    when:
    def result = mockMvc.perform(get("/api/project/${testId}")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())

    then:
    1 * svc.getId(testId) >> null
  }

  def "DELETE for a non existent project name should return NOT_FOUND"() {
    when:
    def result = mockMvc.perform(get("/api/project/${testProjectName}")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())

    then:
    1 * svc.getProjectNamed(testProjectName) >> null
  }

  def "DELETE for a project should attempt to delete it"() {
    given:
    svc.getId(testId) >> expectedProjectInfo

    when:
    mockMvc.perform(delete("/api/project/${testId}")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())

    then:
    1 * svc.delete(expectedProjectInfo)
  }

}

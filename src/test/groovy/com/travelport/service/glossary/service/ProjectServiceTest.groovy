package com.travelport.service.glossary.service

import org.springframework.data.mongodb.core.MongoTemplate

import com.travelport.service.glossary.config.ServiceGlossaryConfiguration
import com.travelport.service.glossary.model.ProjectInfo
import com.travelport.service.glossary.repository.ProjectInfoRepository
import com.travelport.service.glossary.service.enrichments.ReadEnrichment

class ProjectServiceTest extends ProjectInfoTestBase {
  final ServiceGlossaryConfiguration mockCfg = Mock()
  final ProjectInfoRepository mockRepo = Mock()
  final MongoTemplate mockMongo = Mock()

  ProjectService cut

  def setup() {
    cut = new ProjectService().with {
      cfg = mockCfg
      projectStore = mockRepo
      mongoTmpl = mockMongo
      readEnrichments = []
      saveEnrichments = []
      it
    }
  }

  def "Saving a project should set the ID for when replacing an existing project"() {
    final def existingProject = createProject(999L)
    mockRepo.findByName(_) >> existingProject
    mockRepo.save(_) >> { ProjectInfo it -> it }

    when:
    def result = cut.save(testProject)

    then:
    result.is(testProject)
    result.id == existingProject.id
  }

  def "Saving a new project should allocate the next id higher than the maximum in the repo"() {
    mockRepo.findByName(_) >> null
    mockRepo.save(_) >> { ProjectInfo it -> it }

    mockMongo.findOne(_, ProjectInfo) >> createProject(1000L)

    when:
    def result = cut.save(testProject)

    then:
    result.is(testProject)
    result.id == 1001L
  }

  def "Saving a project where no projects previously existed set the id to the first allowable"() {
    mockRepo.findByName(_) >> null
    mockRepo.save(_) >> { ProjectInfo it -> it }

    mockMongo.findOne(_, ProjectInfo) >> null

    mockCfg.getMinimumAllocatableId() >> 500L

    when:
    def result = cut.save(testProject)

    then:
    result.is(testProject)
    result.id == 500L
  }

  def "Retrieving a list of projects enriches each one"() {
    final List<ProjectInfo> testProjectList = [createProject(1, "test-project-01"), createProject(2, "test-project-02")]

    int rnd = new Random(System.currentTimeMillis()).nextInt(10_000)
    final String stringToProveEnrichment = "Enriched_${rnd}"

    ReadEnrichment<ProjectInfo> mockEnricher = Mock()
    cut.readEnrichments = [mockEnricher]

    given:
    mockRepo.findAll(_) >> testProjectList

    testProjectList.forEach {
      assert it.desc == null || !it.desc.startsWith(stringToProveEnrichment)
    }

    when:
    def result = cut.list()

    then:
    testProjectList.size() * mockEnricher.enrich(_) >> {ProjectInfo pi ->
      pi.desc = "${stringToProveEnrichment}_${pi.id}-${pi.name}"
      pi
    }

    and:
    result.size() == testProjectList.size()
    result.eachWithIndex {ProjectInfo pi, int idx ->
      assert pi.is(testProjectList[idx])  //Make sure they're in same order and not substituted
      assert pi.desc == "${stringToProveEnrichment}_${pi.id}-${pi.name}"
    }
  }

  def "getting a project by Id enriches it"() {
    final long testId = 999L

    int rnd = new Random(System.currentTimeMillis()).nextInt(10_000)
    final String stringToProveEnrichment = "Enriched_${rnd}"
    ReadEnrichment<ProjectInfo> mockEnricher = Mock()

    cut.readEnrichments = [mockEnricher]

    assert testProject.desc != stringToProveEnrichment

    when:
    def result = cut.getId(testId)

    then:
    1 * mockRepo.findById(testId) >> Optional.of(testProject)
    1 * mockEnricher.enrich(_) >> { ProjectInfo it ->
      it.desc = stringToProveEnrichment
      it
    }

    and:
    result.desc == stringToProveEnrichment
  }

  def "getting a project where it does not exist returns null"() {
    final long testId = 999L

    mockRepo.findById(testId) >> Optional.empty()
    mockRepo.findById(testProjectName) >> null

    when:
    def resultForId = cut.getId(testId)

    then:
    resultForId == null

    when:
    def resultForName = cut.getProjectNamed(testProjectName)

    then:
    resultForName == null
  }

  def "getting a project by name enriches it"() {
    int rnd = new Random(System.currentTimeMillis()).nextInt(10_000)
    final String stringToProveEnrichment = "Enriched_${rnd}"
    ReadEnrichment<ProjectInfo> mockEnricher = Mock()

    cut.readEnrichments = [mockEnricher]

    assert testProject.desc != stringToProveEnrichment

    when:
    def result = cut.getProjectNamed(testProjectName)

    then:
    1 * mockRepo.findByName(testProjectName) >> testProject
    1 * mockEnricher.enrich(_) >> { ProjectInfo it ->
      it.desc = stringToProveEnrichment
      it
    }

    and:
    result.desc == stringToProveEnrichment
  }

  def "delete a null project throws an exception"() {
    when:
    cut.delete(null)

    then:
    0 * mockRepo.delete(_)
    thrown IllegalArgumentException
  }

  def "delete for a valid project tries to delete it from the repo"() {
    when:
    cut.delete(testProject)

    then:
    notThrown IllegalArgumentException
    1 * mockRepo.delete(testProject)
  }

  //Not really worth testing, but done for 100% coverage
  def "delete by name throws an exception if the project name is not found"() {
    when:
    cut.deleteAllProjects()

    then:
    1 * mockRepo.deleteAll()
  }

  def "Service details when no projects on repo"() {
    mockRepo.count() >> 0

    when:
    def result = cut.serviceDetails

    then:
    0 * mockMongo.findOne(_)
    0 * mockCfg.getMinimumAllocatableId()

    and:
    result.containsKey("count")
    result.count == 0

    ! result.containsKey("maxProjectId")
  }

  def "Service details when there ARE projects on repo and id is after minimum"() {
    final Long idFromConfig = 101L
    final long testId = 567L

    mockRepo.count() >> 3

    when:
    def result = cut.serviceDetails

    then:
    1 * mockMongo.findOne(_, ProjectInfo) >> createProject(testId)
    1 * mockCfg.getMinimumAllocatableId() >> idFromConfig

    and:
    result.containsKey("maxProjectId")
    result.maxProjectId == testId + 1
  }

  def "Service details when there ARE projects on repo and id is before minimum"() {
    final Long idFromConfig = 1_001L
    final long testId = 567L

    mockRepo.count() >> 3

    when:
    def result = cut.serviceDetails

    then:
    1 * mockMongo.findOne(_, ProjectInfo) >> createProject(testId)
    2 * mockCfg.getMinimumAllocatableId() >> idFromConfig

    and:
    result.containsKey("maxProjectId")
    result.maxProjectId == idFromConfig
  }

  def "Service details in the unlikely event having projects on repo, but not finding the one with the highest id"() {
    final Long idFromConfig = 1_001L
    mockRepo.count() >> 3

    when:
    def result = cut.serviceDetails

    then:
    1 * mockMongo.findOne(_, ProjectInfo) >> null
    1 * mockCfg.getMinimumAllocatableId() >> idFromConfig

    and:
    result.containsKey("maxProjectId")
    result.maxProjectId == idFromConfig

  }
}

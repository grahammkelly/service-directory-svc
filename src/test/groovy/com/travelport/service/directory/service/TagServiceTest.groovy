package com.travelport.service.directory.service

import com.travelport.service.directory.config.ServiceDirectoryConfiguration
import com.travelport.service.directory.model.Tag
import com.travelport.service.directory.repository.ProjectInfoRepository
import com.travelport.service.directory.repository.TagRepository
import spock.lang.Specification

class TagServiceTest extends Specification {
  final TagRepository mockTagRepo = Mock()
  final ProjectInfoRepository mockProjectRepo = Mock()

  TagService cut

  def setup(){
    cut = new TagService().with {
      tagRepo = mockTagRepo
      projectRepo = mockProjectRepo
      it
    }
  }

  private def createdNewTag(String newName, List<String> newProjects = []) {
    new Tag(newName, newProjects.toSet())
  }

  def "Retrieving a non-existing tag should return null"() {
    mockTagRepo.findById(_) >> Optional.empty()

    when:
    def result = cut.findTag("test")

    then:
    result == null
  }

  def "Storing a tag with associated projects works"() {
    final def testTag = createdNewTag("test-tag-with-projects", ["test-project"])

    when:
    cut.save(testTag)

    then:
    1 * mockTagRepo.save(_)
  }

  def "Storing a tag with no associated projects should delete an existing one or just ignore"() {
    final def testTag = createdNewTag("test-tag")

    mockTagRepo.findById(_) >> Optional.ofNullable(existingTag)

    when:
    def result = cut.save(testTag)

    then:
    (expectDelete ? 1: 0) * mockTagRepo.delete(existingTag)

    and:
    result == null

    where:
    existingTag || expectDelete
    null || false
    createdNewTag("test-tag", ["blah-project"]) || true

  }

  def "Clear the tag repo makes sure the project repo is empty first"() {
    when:
    cut.clear()

    then:
    1 * mockProjectRepo.count() >> 0

    and:
    1 * mockTagRepo.deleteAll()
  }

  def "Clear the tag repo fails if there are still rojects on the project repo"() {
    mockProjectRepo.count() >> 5

    when:
    cut.clear()

    then:
    thrown IllegalStateException

    and:
    0 * mockTagRepo.deleteAll()
  }

  def "Getting service details will give a count of tags"() {
    when:
    def result = cut.serviceDetails

    then:
    result.size() > 0
    result.containsKey("count")
  }

}

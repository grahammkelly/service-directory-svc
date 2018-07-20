package com.mttnow.system.team.service

import com.mttnow.platform.spring.boot.test.auto.configure.mongo.NeedsCleanUp
import com.mttnow.platform.spring.boot.test.auto.configure.mongo.NeedsTestData
import com.mttnow.system.team.IntegrationTest
import com.mttnow.system.team.model.Person
import org.springframework.beans.factory.annotation.Autowired

class PersonServiceIntegrationTest extends IntegrationTest {

  @Autowired
  PersonService personService

  @NeedsCleanUp
  def "Should create a new person"() {

    given:
    Person person = readSamplePerson()

    when:
    personService.save(person)

    then:
    personService.count() == 1

  }

  @NeedsTestData("data/persons.json")
  def "Should read all persons"() {

    when:
    def persons = personService.findAll()

    then:
    persons.size() == 3
  }

}

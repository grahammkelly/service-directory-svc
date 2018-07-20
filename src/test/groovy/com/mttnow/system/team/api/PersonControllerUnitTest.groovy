package com.mttnow.system.team.api

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.mttnow.system.team.UnitTest
import com.mttnow.system.team.model.Person
import com.mttnow.system.team.service.PersonService

class PersonControllerUnitTest extends UnitTest {

  @Subject
  PersonController personController

  @Collaborator
  PersonService personService = Mock()

  def "Should call findAll"() {

    given:
    def persons = [ new Person(firstName: "John") ]

    when:
    def result = personController.findAll()

    then:
    1 * personService.findAll() >> persons

    result == persons

  }

  def "Should call publish"() {

    given:
    def person = new Person(firstName: "John")

    when:
    personController.addPerson(person)

    then:
    1 * personService.publish(person)

  }

}

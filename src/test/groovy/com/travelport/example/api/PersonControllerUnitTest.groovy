package com.travelport.example.api

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.travelport.example.UnitTest
import com.travelport.example.model.Person
import com.travelport.example.service.PersonService

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

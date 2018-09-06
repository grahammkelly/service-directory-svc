package com.travelport.example.service

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.travelport.example.UnitTest
import com.travelport.example.model.Person
import com.travelport.example.repository.PersonRepository

class PersonServiceUnitTest extends UnitTest {

  @Subject
  PersonService personService

  @Collaborator
  PersonRepository personRepository = Mock()

  @Collaborator
  PersonPublisher personPublisher = Mock()

  def "Should call count"() {

    given:
    def count = 10

    when:
    def result = personService.count()

    then:
    1 * personRepository.count() >> count

    result == count

  }

  def "Should call findAll"() {

    given:
    def persons = [ new Person(firstName: "John") ]

    when:
    def result = personService.findAll()

    then:
    1 * personRepository.findAll() >> persons

    result == persons

  }

  def "Should call save"() {

    given:
    def person = new Person(firstName: "John")

    when:
    personService.save(person)

    then:
    1 * personRepository.save(person)

  }

  def "Should call publish"() {

    given:
    def person = new Person(firstName: "John")

    when:
    personService.publish(person)

    then:
    1 * personPublisher.publish(person)

  }

}

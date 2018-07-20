package com.mttnow.system.team.api

import com.mttnow.system.team.IntegrationTest
import com.mttnow.system.team.model.Person
import com.mttnow.system.team.service.PersonService
import org.spockframework.spring.SpringBean
import org.springframework.http.HttpStatus

class PersonControllerIntegrationTest extends IntegrationTest {

  @SpringBean
  PersonService personService = Mock()

  def "Should be bad request if firstName is empty"() {

    when:
    def response = testRestTemplate.postForEntity("/persons", new Person(), Void.class)

    then:
    response.statusCode == HttpStatus.BAD_REQUEST

    0 * personService.publish(_)

  }

  def "Should create Person with firstName only"() {

    given:
    def person = new Person(firstName: "John")

    when:
    def response = testRestTemplate.postForEntity("/persons", person, Void.class)

    then:
    response.statusCode == HttpStatus.CREATED

    1 * personService.publish(_)

  }

  def "Should read all persons"() {

    when:
    def response = testRestTemplate.getForEntity("/persons", Person[].class)

    then:
    1 * personService.findAll() >> {
      [
          new Person(id: "id", firstName: "John", lastName: "Smith", age: 28)
      ]
    }

    response.statusCode == HttpStatus.OK

    def persons = response.body
    persons.size() == 1

    def first = persons.first()

    first.id == "id"
    first.firstName == "John"
    first.lastName == "Smith"
    first.age == 28
  }

}

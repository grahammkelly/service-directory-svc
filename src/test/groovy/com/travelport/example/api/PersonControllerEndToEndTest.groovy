package com.travelport.example.api

import com.mttnow.platform.spring.boot.test.auto.configure.mongo.NeedsCleanUp
import com.mttnow.platform.spring.boot.test.auto.configure.mongo.NeedsTestData
import com.mttnow.platform.spring.boot.test.auto.configure.rabbit.setup.NeedsRabbitListener
import com.travelport.example.IntegrationTest
import com.travelport.example.config.RabbitConfig
import com.travelport.example.model.Person
import com.travelport.example.repository.PersonRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import spock.lang.Ignore

import java.util.concurrent.TimeUnit

import static com.jayway.awaitility.Awaitility.await
import static org.hamcrest.Matchers.is

class PersonControllerEndToEndTest extends IntegrationTest {

  @Autowired
  PersonRepository personRepository

  @NeedsCleanUp
  @NeedsRabbitListener(RabbitConfig.PERSON_QUEUE)
  def "Should create new person"() {

    given:
    Person person = readSamplePerson()

    when:
    def response = testRestTemplate.postForEntity("/persons", person, Void.class)

    await().atMost(2, TimeUnit.SECONDS)
        .until({ personRepository.count() }, is(1L))

    then:
    response.statusCode == HttpStatus.CREATED

    Person personInserted = personRepository.findAll().first()

    person.firstName == personInserted.firstName
    person.lastName == personInserted.lastName
    person.age == personInserted.age
  }

  @NeedsTestData("data/persons.json")
  def "Should read all persons"() {

    when:
    def response = testRestTemplate.getForEntity("/persons", Person[].class)

    then:
    response.statusCode == HttpStatus.OK

    def persons = response.body
    persons.size() == 3

    def first = persons.first()

    first.id == "1"
    first.firstName == "John"
    first.lastName == "Smith"
    first.age == 28
  }

}

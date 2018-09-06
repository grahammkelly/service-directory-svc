package com.travelport.example.service

import com.travelport.example.IntegrationTest
import com.travelport.example.config.RabbitConfig
import com.travelport.example.model.Person
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired

class PersonPublisherIntegrationTest extends IntegrationTest {

  @Autowired
  PersonPublisher personPublisher

  @Autowired
  RabbitTemplate personAmqpTemplate

  def "Should publish person"() {

    given:
    Person person = readSamplePerson()

    when:
    personPublisher.publish(person)

    then:
    Person personQueued = personAmqpTemplate.receiveAndConvert(RabbitConfig.PERSON_QUEUE) as Person

    person == personQueued
  }

}

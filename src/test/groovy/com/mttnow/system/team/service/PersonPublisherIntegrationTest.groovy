package com.mttnow.system.team.service

import com.mttnow.system.team.IntegrationTest
import com.mttnow.system.team.config.RabbitConfig
import com.mttnow.system.team.model.Person
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

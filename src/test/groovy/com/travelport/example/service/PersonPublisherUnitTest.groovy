package com.travelport.example.service

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.travelport.example.UnitTest
import com.travelport.example.model.Person
import org.springframework.amqp.rabbit.core.RabbitTemplate

class PersonPublisherUnitTest extends UnitTest {

  @Subject
  PersonPublisher personPublisher

  @Collaborator
  RabbitTemplate personAmqpTemplate = Mock()

  def "Should call convertAndSend"() {

    given:
    def person = new Person(firstName: "John")

    when:
    personPublisher.publish(person)

    then:
    1 * personAmqpTemplate.convertAndSend(person)

  }

}

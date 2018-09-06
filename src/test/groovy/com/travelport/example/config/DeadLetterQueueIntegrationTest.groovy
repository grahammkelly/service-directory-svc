package com.travelport.example.config

import com.mttnow.platform.spring.boot.test.auto.configure.rabbit.setup.NeedsRabbitListener
import com.travelport.example.IntegrationTest
import com.travelport.example.model.Person
import com.travelport.example.service.PersonService
import org.spockframework.spring.SpringBean
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import spock.util.concurrent.AsyncConditions

class DeadLetterQueueIntegrationTest extends IntegrationTest {

  @Autowired
  RabbitTemplate personAmqpTemplate

  @SpringBean
  PersonService personService = Mock()

  @NeedsRabbitListener(RabbitConfig.PERSON_QUEUE)
  def "Should retry on runtime exception and message should appear on dead letter queue"() {

    def asyncCondition = new AsyncConditions(3)

    given:
    def person = this.readSamplePerson()

    personService.save(_) >> { Person argument ->
      asyncCondition.evaluate {
        assert argument == person
      }
      throw new RuntimeException("Force exception to happen")
    }

    when:
    personAmqpTemplate.convertAndSend(person)

    then:
    asyncCondition.await(5)

    Person personQueued =
        personAmqpTemplate.receiveAndConvert(RabbitConfig.PERSON_QUEUE_DEAD_LETTER)

    person == personQueued
  }

}

package com.mttnow.system.team.service

import com.mttnow.platform.spring.boot.test.auto.configure.rabbit.setup.NeedsRabbitListener
import com.mttnow.system.team.IntegrationTest
import com.mttnow.system.team.config.RabbitConfig
import com.mttnow.system.team.model.Person
import org.spockframework.spring.SpringBean
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import spock.util.concurrent.AsyncConditions

class PersonListenerIntegrationTest extends IntegrationTest {

  @Autowired
  RabbitTemplate personAmqpTemplate

  @SpringBean
  PersonService personService = Mock()

  @NeedsRabbitListener(RabbitConfig.PERSON_QUEUE)
  def "Should consume Person"() {

    def asyncCondition = new AsyncConditions()

    given:
    Person person = readSamplePerson()

    personService.save(_) >> { Person argument ->
      asyncCondition.evaluate() {
        assert argument == person
      }
    }

    when:
    personAmqpTemplate.convertAndSend(person)

    then:
    asyncCondition.await()

  }


}

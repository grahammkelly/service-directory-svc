package com.mttnow.system.team.service

import com.mttnow.system.team.model.Person
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PersonPublisher {

  @Autowired
  private lateinit var personAmqpTemplate: RabbitTemplate

  fun publish(person: Person) {
    personAmqpTemplate.convertAndSend(person)
  }
}

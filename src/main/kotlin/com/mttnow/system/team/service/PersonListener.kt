package com.mttnow.system.team.service

import com.mttnow.system.team.config.RabbitConfig
import com.mttnow.system.team.model.Person
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PersonListener {

  @Autowired
  lateinit var personService: PersonService

  @RabbitListener(id = RabbitConfig.PERSON_QUEUE, queues = [RabbitConfig.PERSON_QUEUE],
      containerFactory = RabbitConfig.PERSON_LISTENER_FACTORY)
  fun consume(person: Person) {
    personService.save(person)
  }
}

package com.mttnow.system.team.service;

import com.mttnow.system.team.model.Person;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonPublisher {

  @Autowired
  private RabbitTemplate personAmqpTemplate;

  public void publish(Person person) {
    personAmqpTemplate.convertAndSend(person);
  }

}

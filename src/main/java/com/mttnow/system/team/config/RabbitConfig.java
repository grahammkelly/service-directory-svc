package com.mttnow.system.team.config;

import com.mttnow.platform.spring.boot.auto.configure.rabbit.RabbitConfigurerAdapter;
import com.mttnow.system.team.model.Person;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig extends RabbitConfigurerAdapter {

  public static final String PERSON_QUEUE = "person.queue";
  public static final String PERSON_QUEUE_DEAD_LETTER = "person.queue.dl";
  public static final String PERSON_LISTENER_FACTORY = "personListenerFactory";
  
  @Bean
  protected Queue personQueue() {
    return new Queue(PERSON_QUEUE, true);
  }

  @Bean
  protected Queue personDeadLetterQueue() {
    return new Queue(PERSON_QUEUE_DEAD_LETTER, true);
  }

  @Bean
  protected RabbitTemplate personAmqpTemplate() {
    RabbitTemplate rabbitTemplate = createRabbitTemplate();

    rabbitTemplate.setRoutingKey(PERSON_QUEUE);
    rabbitTemplate.setMessageConverter(messageConverter(Person.class));

    return rabbitTemplate;
  }

  @Bean(name = RabbitConfig.PERSON_LISTENER_FACTORY)
  protected SimpleRabbitListenerContainerFactory personListenerContainerFactory() {
    SimpleRabbitListenerContainerFactory factory =
            createContainerFactoryWithRetryInterceptor(personQueue(), personDeadLetterQueue());

    factory.setMessageConverter(messageConverter(Person.class));

    return factory;
  }

}

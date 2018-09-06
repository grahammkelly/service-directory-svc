package com.travelport.example.config

import com.mttnow.platform.spring.boot.auto.configure.rabbit.RabbitConfigurerAdapter
import com.travelport.example.model.Person
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig : RabbitConfigurerAdapter() {

  @Bean
  protected fun personQueue(): Queue {
    return Queue(PERSON_QUEUE, true)
  }

  @Bean
  protected fun personDeadLetterQueue(): Queue {
    return Queue(PERSON_QUEUE_DEAD_LETTER, true)
  }

  @Bean
  protected fun personAmqpTemplate(): RabbitTemplate {
    val rabbitTemplate = createRabbitTemplate()

    rabbitTemplate.routingKey = PERSON_QUEUE
    rabbitTemplate.messageConverter = messageConverter(Person::class.java)

    return rabbitTemplate
  }

  @Bean(name = [RabbitConfig.PERSON_LISTENER_FACTORY])
  protected fun personListenerContainerFactory(): SimpleRabbitListenerContainerFactory {
    val factory = createContainerFactoryWithRetryInterceptor(personQueue(), personDeadLetterQueue())

    factory.setMessageConverter(messageConverter(Person::class.java))

    return factory
  }

  companion object {
    const val PERSON_QUEUE = "person.queue"
    const val PERSON_QUEUE_DEAD_LETTER = "person.queue.dl"
    const val PERSON_LISTENER_FACTORY = "personListenerFactory"
  }
}

package com.mttnow.system.team.service

import com.mttnow.system.team.model.Person
import com.mttnow.system.team.repository.PersonRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PersonService {

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var personPublisher: PersonPublisher

  fun count(): Long {
    return personRepository.count()
  }

  fun findAll(): List<Person> {
    return personRepository.findAll()
  }

  fun save(person: Person) {
    personRepository.save(person)
  }

  fun publish(person: Person) {
    personPublisher.publish(person)
  }
}

package com.mttnow.system.team.service;

import com.mttnow.system.team.model.Person;
import com.mttnow.system.team.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonService {

  @Autowired
  private PersonRepository personRepository;

  @Autowired
  private PersonPublisher personPublisher;

  public long count() {
    return personRepository.count();
  }

  public List<Person> findAll() {
    return personRepository.findAll();
  }

  public void save(Person person) {
    personRepository.save(person);
  }

  public void publish(Person person) {
    personPublisher.publish(person);
  }

}

package com.mttnow.system.team.api;

import com.mttnow.system.team.model.Person;
import com.mttnow.system.team.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/persons")
public class PersonController {

  @Autowired
  private PersonService personService;

  @GetMapping
  public List<Person> findAll() {
    return personService.findAll();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public void addPerson(@Valid @RequestBody Person person) {
    personService.publish(person);
  }

}

package com.travelport.example.api

import com.travelport.example.model.Person
import com.travelport.example.service.PersonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid

@RestController
@RequestMapping("/persons")
class PersonController {

  @Autowired
  private lateinit var personService: PersonService

  @GetMapping
  fun findAll(): List<Person> {
    return personService.findAll()
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun addPerson(@Valid @RequestBody person: Person) {
    personService.publish(person)
  }
}

package com.travelport.example.api

import com.travelport.example.model.Person
import com.travelport.example.service.PersonService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(PersonController.class)
class PersonControllerWebMvcTest extends Specification {

  @Autowired
  MockMvc mockMvc

  @SpringBean
  PersonService personService = Mock()

  def "Should be bad request if firstName is empty"() {

    when:
    mockMvc.perform(post("/persons")
        .contentType(MediaType.APPLICATION_JSON)
        .content('{ "firstName": "" }')
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())

    then:
    0 * personService.publish(_)

  }

  def "Should create Person with firstName only"() {

    when:
    mockMvc.perform(post("/persons")
        .contentType(MediaType.APPLICATION_JSON)
        .content('{ "firstName": "John" }')
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())

    then:
    1 * personService.publish(_)

  }

  def "Should read all persons"() {

    when:
    mockMvc.perform(get("/persons")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(jsonPath('$.length()').value(1))
        .andExpect(jsonPath('$[0].id').value('id'))
        .andExpect(jsonPath('$[0].firstName').value('John'))
        .andExpect(jsonPath('$[0].lastName').value('Smith'))
        .andExpect(jsonPath('$[0].age').value(28))

    then:
    1 * personService.findAll() >> {
      [
        new Person(id: "id", firstName: "John", lastName: "Smith", age: 28)
      ]
    }

  }

}

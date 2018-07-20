package com.mttnow.system.team.model

import com.mttnow.platform.spring.boot.auto.configure.jackson.JacksonDefaultAutoConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.boot.test.json.JsonContent
import org.springframework.core.io.ClassPathResource
import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat

@JsonTest
@ImportAutoConfiguration(JacksonDefaultAutoConfiguration.class)
class PersonJsonTest extends Specification {

  @Autowired
  JacksonTester<Person> json

  def "Can serialize all properties"() {

    given:
    Person person = new Person(
      id: "id",
      firstName: "firstName",
      lastName: "lastName",
      age: 32
    )

    when:
    JsonContent<Person> result = this.json.write(person)

    then:
    assertThat(result)
        .extractingJsonPathValue("@.id").isEqualTo("id")

    assertThat(result)
        .extractingJsonPathValue("@.firstName").isEqualTo("firstName")

    assertThat(result)
        .extractingJsonPathValue("@.lastName").isEqualTo("lastName")

    assertThat(result)
        .extractingJsonPathValue("@.age").isEqualTo(32)

  }

  def "Can deserialize all properties"() {

    when:
    Person person = this.json.readObject(new ClassPathResource("data/samplePerson.json"))

    then:

    person.id == "id"
    person.firstName == "Steve"
    person.lastName == "Jobs"
    person.age == 33

  }

}

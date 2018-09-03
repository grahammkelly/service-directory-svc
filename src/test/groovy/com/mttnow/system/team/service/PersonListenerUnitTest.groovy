package com.mttnow.system.team.service

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import com.mttnow.system.team.UnitTest
import com.mttnow.system.team.model.Person

class PersonListenerUnitTest extends UnitTest {

  @Subject
  PersonListener personListener

  @Collaborator
  PersonService personService = Mock()

  def "Should call save"() {

    given:
    //Person is now a kotlin class, cannot use Groovy named arguments
    def person = new Person("", "John", "", 0)

    when:
    personListener.consume(person)

    then:
    1 * personService.save(person)

  }

}

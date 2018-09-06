package com.travelport.example

import com.mttnow.platform.spring.boot.test.auto.configure.SpringBootIntegrationTest
import com.mttnow.platform.spring.boot.test.auto.configure.util.JsonReaderUtils
import com.travelport.example.model.Person
import com.xebialabs.restito.server.StubServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import spock.lang.Specification

@SpringBootIntegrationTest
abstract class IntegrationTest extends Specification {

  @Autowired
  protected TestRestTemplate testRestTemplate

  @Autowired
  protected StubServer stubServer

  Person readSamplePerson() {
    JsonReaderUtils.readJson("data/samplePerson.json", Person.class)
  }

}

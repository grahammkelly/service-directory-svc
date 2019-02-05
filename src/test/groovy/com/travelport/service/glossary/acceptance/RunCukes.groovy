package com.travelport.service.glossary.acceptance

import com.travelport.Application
import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext

@RunWith(Cucumber)
@CucumberOptions(
    strict = true,
    features = ["src/test/resources/features"],
    glue = ["src/test/groovy/com/travelport/service/glossary/acceptance/steps"],
    tags = ["not @Ignore"]
)
class RunCukes {

  static ConfigurableApplicationContext app

  @BeforeClass
  static void setUp() {
    System.setProperty('spring.profiles.active', ServerConfig.springActiveProfile)
    System.setProperty('server.port', ServerConfig.serverPort as String)
    System.setProperty('server.contextPath', ServerConfig.serverContextPath)

    app = SpringApplication.run(Application.class)
  }

  @AfterClass
  static void tearDown() {
    app.stop()
  }

}

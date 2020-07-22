package com.travelport.service.glossary.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "service-glossary")
data class ServiceGlossaryConfiguration (
  var git: GitInfo = GitInfo(),
  var minimumAllocatableId: Long = 1_000L,
  var jenkinsAddress: String = "https://system-jenkins.build.mttnow.com",  //Default to jenkins on the MTT build account
  var serviceName: String = "Service Glossary",
  var sonar: SonarInfo = SonarInfo(),
  var network: NetworkConfig = NetworkConfig()
)

data class GitInfo (
  var baseAddr: String = ""
)

data class SonarInfo(
  var username: String = "jenkins",
  var passwd: String = "jenkins",
  var baseAddr: String = "http://sonar2.int.build.mttnow.com"
)

data class NetworkConfig(
    var connectionTimeout: Int = 500,
    var readTimeout: Int = 30_000
)


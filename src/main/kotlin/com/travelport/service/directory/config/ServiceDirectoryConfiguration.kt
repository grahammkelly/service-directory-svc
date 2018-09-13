package com.travelport.service.directory.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "service-directory")
data class ServiceDirectoryConfiguration (
  var git: GitInfo = GitInfo(""),
  var minimumAllocatableId: Long = 1_000L
)

data class GitInfo (
  var baseAddr: String = ""
)


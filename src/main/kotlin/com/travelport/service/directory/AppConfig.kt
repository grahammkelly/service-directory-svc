package com.travelport.service.directory

import com.mttnow.jsonobjectmapper.ApiObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.representer.Representer

@Configuration
class AppConfig {
  @Bean
  fun createJsonObjectMapper() : ApiObjectMapper {
    return ApiObjectMapper()
  }

  @Bean
  fun createYamlObjectMapper() : Yaml {
    val representer = Representer();
    representer.propertyUtils.isSkipMissingProperties = true
    return Yaml(representer)
  }
}

package com.travelport.service.glossary

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.mttnow.jsonobjectmapper.DateTimeDeserializer
import com.mttnow.jsonobjectmapper.DateTimeSerializer
import com.mttnow.platform.common.client.impl.CorrelationIdHttpRequestInterceptor
import com.mttnow.platform.common.client.impl.TenantIdHttpRequestInterceptor
import com.travelport.service.glossary.config.ServiceGlossaryConfiguration
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.representer.Representer

@Configuration
class AppConfig {
  @Bean
  fun createJsonObjectMapper() : ObjectMapper {
    val om = ObjectMapper()

    val module = JodaModule()
    module.addDeserializer(DateTime::class.java, DateTimeDeserializer())
    module.addSerializer(DateTime::class.java, DateTimeSerializer())

    om.registerModule(module)
    om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    return om
  }

  @Bean
  fun createYamlObjectMapper() : Yaml {
    val representer = Representer();
    representer.propertyUtils.isSkipMissingProperties = true
    return Yaml(representer)
  }

  @Bean
  fun createRestTemplate(@Autowired cfg: ServiceGlossaryConfiguration) : RestTemplate {
    return RestTemplateBuilder()
        .setConnectTimeout(cfg.network.connectionTimeout)
        .setReadTimeout(cfg.network.readTimeout)
        .additionalInterceptors(
            listOf(TenantIdHttpRequestInterceptor(null), CorrelationIdHttpRequestInterceptor())
        ).build()
  }
}

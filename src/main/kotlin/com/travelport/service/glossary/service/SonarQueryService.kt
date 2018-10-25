package com.travelport.service.glossary.service

import com.travelport.service.glossary.config.ServiceGlossaryConfiguration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Service
class SonarQueryService {
  private val log = LoggerFactory.getLogger(SonarQueryService::class.java)

  @Autowired private lateinit var cfg: ServiceGlossaryConfiguration
  @Autowired private lateinit var restTemplate: RestTemplate

  private val metricsQry: String by lazy {
    "${cfg.sonar.baseAddress.replace("/$".toRegex(), "")}/api/measures/component"
  }

  fun getCoveragePercent(projectKey: String?): Float? {
    if (projectKey != null && projectKey.isNotEmpty()) {
      val url = "$metricsQry?projectKey=$projectKey&metricKey=coverage"
      return try {
        val resp = restTemplate.getForObject(url, MetricsResult::class.java)
        resp.component.measures.get(0)?.value
      } catch (e: RestClientException) {
        log.warn("Unable to retrieve sonar metrics for '{}' - {}", projectKey, e.localizedMessage)
        0.0F
      }
    }
    return null
  }

  data class MetricsResult(
      val component: Component
  )

  data class Component(
      val id: String,
      val key: String,
      val measures: List<Measure>
  )

  data class Measure(
      val metric: String,
      val value: Float
  )
}

package com.travelport.service.directory.info.contributors

import com.travelport.service.directory.metrics.Metrics
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component

@Component
class MetricsInformation: InfoContributor {
  @Autowired private lateinit var metricsSvc: Metrics

  val startTime = DateTime.now()

  val pf = PeriodFormatterBuilder()
      .appendDays().appendSuffix(" days ")
      .appendHours().appendSuffix(" hours ")
      .appendMinutes().appendSuffix(" minutes ")
      .appendSecondsWithMillis().appendSuffix(" seconds")
      .toFormatter()

  override fun contribute(builder: Info.Builder) {
    val period = Period(startTime, DateTime.now())
    builder.withDetail("timeAlive", pf.print(period))
    if (metricsSvc.isEnabled) {
      builder.withDetail("metrics", metricsSvc.metrics)
    }
  }
}

package com.travelport.service.directory.metrics

import org.joda.time.DateTime
import org.joda.time.Period
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class MetricsFilter: Filter {
  @Autowired private lateinit var metrics: Metrics

  override fun init(filterConfig: FilterConfig?) = metrics.recordApplicationEvent("ApplicationStart")

  override fun destroy() = metrics.recordApplicationEvent("ApplicationStop")

  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {

    val req = request as HttpServletRequest
    val startTime = DateTime.now()

    chain.doFilter(request, response)

    val resp = response as HttpServletResponse
    var eventKey = MDC.get("overrideEventKey")
    if (eventKey == null || eventKey.isEmpty()) {
      eventKey = "${req.method} ${req.requestURI}"
    } else {
      MDC.remove("overrideEventKey")  //Remove from the MDC here, as it's not done at the same level as it was set
    }
    metrics.recordHttpEvent(eventKey, Period(startTime, DateTime.now()).millis, resp.status)
  }
}

abstract class MeasuringService {
  protected fun setEventName(httpMethod: HttpMethod, uri: String)  {
    //We're not worrying about unsetting the MDC, as it's used OUTSIDE the context of the HTTP calls (by the surrounding MetricsFilter)!
    MDC.put("overrideEventKey", "${httpMethod} ${uri}")
  }
}


package com.travelport.service.glossary.metrics

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.springframework.http.HttpMethod
import spock.lang.Specification

class MetricsFilterTest extends Specification {
  Metrics mockMetrics = Mock()

  MetricsFilter cut

  def setup() {
    cut = new MetricsFilter().with {
      metrics = mockMetrics
      return it
    }
  }

  def "Initialisation should trigger a metroics event"() {
    when:
    cut.init(null)

    then:
    //Kotlin code substitutes functons where the body is an expression with the expression itself
    // So, while the code shows metrics.recordApplicationEvent, follow the equals to get the real function call!
    1 * mockMetrics.recordEvent(_, _, _) >> {String arg, Integer milis, EventResult result ->
      assert arg.toLowerCase().contains("start")
    }
  }

  def "Stop should triggers metrics event"() {
    when:
    cut.destroy()

    then:
    1 * mockMetrics.recordEvent(_, _, _) >> {String arg, Integer milis, EventResult result ->
      assert arg.toLowerCase().contains("stop")
    }
  }

  def "Intercepted servlet call should record the start of the method, and report duration of the call"() {
    def minDelayMillis = 2 //seconds
    FilterChain mockFilter = Mock()

    when:
    cut.doFilter(Mock(HttpServletRequest), Mock(HttpServletResponse), mockFilter)

    then:
    1 * mockFilter.doFilter(_, _) >> {req, resp ->
      Thread.sleep(minDelayMillis)
    }

    and:
    1 * mockMetrics.recordEvent(_, _, _) >> {String event, Integer duration, EventResult result ->
      assert duration >= minDelayMillis
    }
  }

  def "Event to measure is defaulted from the http request"() {
    String testMethod = "TEST"
    String testUri = "/uri"

    HttpServletRequest mockReq = Mock()
    HttpServletResponse mockResp = Mock()

    given:
    mockReq.getMethod() >> testMethod
    mockReq.getRequestURI() >> testUri

    mockResp.getStatus() >> 200

    when:
    cut.doFilter(mockReq, mockResp, Mock(FilterChain))

    then:
    1 * mockMetrics.recordEvent(_, _, _) >> {String event, Integer duration, EventResult result ->
      assert event == "${testMethod} ${testUri}"
      assert result == EventResult.SUCCESSFUL
    }
  }

  def "Invoking a measurable service (which overrides event name) should use it's event name, not the default"() {
    String testMethod = "TEST"
    String testUri = "/uri"

    given:
    HttpServletRequest mockReq = Mock()
    mockReq.getMethod() >> testMethod
    mockReq.getRequestURI() >> testUri

    and:
    FilterChain mockFilter = Mock()
    mockFilter.doFilter(_, _) >> {new TestMeasuringService().doSomethingMeasurable()}

    when:
    cut.doFilter(mockReq, Mock(HttpServletResponse), mockFilter)

    then:
    1 * mockMetrics.recordEvent(_, _, _) >> {String event, Integer duration, EventResult result ->
      assert event != "${testMethod} ${testUri}"
      assert event == new TestMeasuringService().expectedEventName
    }
  }

  static class TestMeasuringService extends MeasuringService {
    final def method = HttpMethod.GET
    final def svcUri = "/serviceUri"
    void doSomethingMeasurable() {
      setEventName(method, svcUri)
    }

    String getExpectedEventName() {
      "${method} ${svcUri}"
    }
  }
}

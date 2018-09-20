package com.travelport.service.directory.metrics

import org.springframework.http.HttpStatus

import spock.lang.Specification

class MetricsTest extends Specification {

  def "No-op: Recording an event does exactly nothing"() {
    given:
    final Metrics cut = new NoopMetrics()
    assert cut.metrics.isEmpty()

    when:
    cut.recordEvent("TestEvent", 0, EventResult.SUCCESSFUL)

    then:
    cut.metrics == [:]
  }

  def "Basic: Recording an new event type should add the event type"() {
    given:
    final Metrics cut = new BasicMetrics()
    assert cut.metrics.isEmpty()

    when:
    cut.recordApplicationEvent("TestEvent")

    then:
    cut.metrics.containsKey "TestEvent"
    (cut.metrics.TestEvent as Map).containsKey "SUCCESSFUL"
    cut.metrics.TestEvent.SUCCESSFUL == new BasicMetrics.EventInfo().addEvent(0)

  }

  def "Basic: Recording successful events calculates average duration correctly"() {
    final String testEvent = "test-successful-event"

    given:
    final Metrics cut = new BasicMetrics()

    when: "we record a series of successful events of a specific type"
    durations.each {cut.recordSuccess(testEvent, it)}

    then:
    cut.metrics[testEvent]["SUCCESSFUL"].count == durations.size()
    cut.metrics[testEvent]["SUCCESSFUL"].totalDuration == durations.sum()
    cut.metrics[testEvent]["SUCCESSFUL"].averageDuration == expectedAve

    where:
    durations                 || expectedAve
    [10]                      || 10
    [5, 15]                   || 10
    [3, 6, 9, 12, 15, 18, 21] || 12
  }

  def "Basic: Recording failed events calculates average duration correctly"() {
    final String testEvent = "test-failure-event"

    given:
    final Metrics cut = new BasicMetrics()

    when:
    durations.each {cut.recordFailure(testEvent, it)}

    then:
    cut.metrics[testEvent]["FAILURE"].count == durations.size()
    cut.metrics[testEvent]["FAILURE"].totalDuration == durations.sum()
    cut.metrics[testEvent]["FAILURE"].averageDuration == expectedAve

    where:
    durations           || expectedAve
    [10]                || 10
    [5, 15]             || 10
    [7, 14, 21, 28, 35] || 21
  }

  def "Basic: Recording am HTTP event calculates the Success/failure"() {
    final String testEvent = "test-http-event"

    given:
    final Metrics cut = new BasicMetrics()
    assert cut.metrics.isEmpty()

    when:
    cut.recordHttpEvent(testEvent, 1, status.value())

    then:
    cut.metrics[testEvent][expectedState].count == 1

    where:
    status                           || expectedState
    HttpStatus.OK                    || "SUCCESSFUL"
    HttpStatus.MOVED_PERMANENTLY     || "SUCCESSFUL"
    HttpStatus.TEMPORARY_REDIRECT    || "SUCCESSFUL"
    HttpStatus.BAD_REQUEST           || "FAILURE"
    HttpStatus.CREATED               || "SUCCESSFUL"
    HttpStatus.ACCEPTED              || "SUCCESSFUL"
    HttpStatus.I_AM_A_TEAPOT         || "FAILURE"
    HttpStatus.INTERNAL_SERVER_ERROR || "FAILURE"
    HttpStatus.SERVICE_UNAVAILABLE   || "FAILURE"
  }
}

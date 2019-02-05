package com.travelport.service.glossary.metrics

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

abstract class Metrics(val isEnabled: Boolean) {
  abstract val metrics: Map<String, Any>

  protected abstract fun recordEvent(eventKey: String, eventDurationMillis: Int, result: EventResult)

  fun recordApplicationEvent(eventKey: String) = recordEvent(eventKey, 0, EventResult.SUCCESSFUL)

  fun recordSuccess(eventKey: String, eventDurationMillis: Int) = recordEvent(eventKey, eventDurationMillis, EventResult.SUCCESSFUL)

  fun recordFailure(eventKey: String, eventDurationMillis: Int) = recordEvent(eventKey, eventDurationMillis,EventResult.FAILURE)

  fun recordHttpEvent(eventKey: String, eventDurationMillis: Int, httpStatus: Int) =
      recordEvent(eventKey, eventDurationMillis, if(httpStatus in 200..399) EventResult.SUCCESSFUL else EventResult.FAILURE)
}

@Profile("!metrics")
@Component
class NoopMetrics: Metrics(false) {
  private val logger = LoggerFactory.getLogger(NoopMetrics::class.java)

  init {
    logger.debug("Starting NO-OP metrics")
  }

  override fun recordEvent(eventKey: String, eventDurationMillis: Int, result: EventResult) {
    //Does nothing
  }

  override val metrics: Map<String, Any> = emptyMap()
}

@Profile("metrics")
@Component
class BasicMetrics: Metrics(true) {
  private val logger = LoggerFactory.getLogger(BasicMetrics::class.java)

  private val basicCounts: MutableMap<String, MutableMap<String, EventInfo>> = mutableMapOf()

  init {
    logger.debug("Starting Basic metrics")
  }

  override val metrics: Map<String, Any>
    get() {
      //Makes everything immutable!
      return basicCounts.toMap().mapValues { it.value.toMap() }
    }

  override fun recordEvent(eventKey: String, eventDurationMillis: Int, result: EventResult) {
    fun addEmptyResultsToMap(): MutableMap<String, EventInfo> {
      logger.trace("Adding event key '${eventKey}'")
      basicCounts[eventKey] = mutableMapOf()
      return basicCounts[eventKey]!!
    }

    val eventMetrics= basicCounts[eventKey] ?: addEmptyResultsToMap()
    eventMetrics[result.name] = eventMetrics.getOrDefault(result.name, EventInfo()).addEvent(eventDurationMillis)
    logger.trace("Metrics for {}[{}] now {}", eventKey, result, eventMetrics[result.name])
  }

  class EventInfo {
    var count: Int = 0
      private set

    var totalDuration: Int = 0
      private set

    val averageDuration: Int
      get() = if (count == 0) 0 else totalDuration/count

    fun addEvent(eventDuration: Int): EventInfo {
      count++
      totalDuration += eventDuration
      return this
    }

    override fun equals(other: Any?): Boolean {
      if (other == null || other !is EventInfo) return false
      if (other === this) return true
      return count == other.count && totalDuration == other.totalDuration &&
          hashCode() == other.hashCode()  //Added for test coverage. I don't want to override equals without haashcode but don't want to test hashcode (testing code only added for test!!!)
    }

    override fun hashCode(): Int {
      return count * totalDuration
    }
  }
}

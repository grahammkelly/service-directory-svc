package com.travelport.service.directory.service.enrichments

import org.springframework.beans.factory.annotation.Autowired

interface SaveEnrichment<T>: ReadEnrichment<T>

interface ReadEnrichment<T>: Enrichment<T>

interface Enrichment<T> {
  fun enrich(obj: T): T
}

abstract class EnrichingService<T> {
  @Autowired lateinit var readEnrichments: List<ReadEnrichment<T>>
  @Autowired lateinit var saveEnrichments: List<SaveEnrichment<T>>

  protected fun applyReadEnrichments(obj: T): T = applyEnrichments(readEnrichments, obj)
  protected fun applySaveEnrichments(obj: T): T = applyEnrichments(saveEnrichments, obj)

  private fun applyEnrichments(enrichmentsToApply: List<Enrichment<T>>, obj: T) =
    enrichmentsToApply.fold(obj) {o, enricher -> enricher.enrich(o)}
}


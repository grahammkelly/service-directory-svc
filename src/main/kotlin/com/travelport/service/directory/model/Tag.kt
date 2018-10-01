package com.travelport.service.directory.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "tags")
data class Tag (
    @field:Id var name: String,
    var projects: Set<String> = emptySet()
)

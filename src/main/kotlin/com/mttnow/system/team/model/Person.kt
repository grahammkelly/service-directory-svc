package com.mttnow.system.team.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import javax.validation.constraints.NotEmpty

@Document
data class Person(
    @field:Id var id: String? = "",       //If not 'field:' on annotation, applied to constructor only, not at a field level!
    @field:NotEmpty var firstName: String? = "",
    var lastName: String? = "",
    var age: Int = 0)

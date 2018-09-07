package com.travelport.service.directory.model

import javax.validation.constraints.NotEmpty

data class Owner(
    @field:NotEmpty var name: String = "",
    var contact: ContactInfo = ContactInfo()
)

package com.travelport.service.glossary.model

import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

data class ProjectTeam @JvmOverloads constructor(
    @field:NotEmpty var name: String? = null,
    var contact: ContactInfo = ContactInfo()
)

data class ContactInfo @JvmOverloads constructor(
    var slack: String? = null,
    var msteams: MSTeamsContactInfo = MSTeamsContactInfo("", ""),
    @field:Email var email: String? = null
)

data class MSTeamsContactInfo @JvmOverloads constructor(
    var name: String? = null,

    @field:Pattern(regexp = "https?://teams.microsoft.com/l/channel/.*", message = "Must be a valid link to an MS Teams channel")
    var link: String? = null
)


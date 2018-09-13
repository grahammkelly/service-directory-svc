package com.travelport.service.directory.model

import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

data class ProjectTeam (
    @field:NotEmpty var name: String? = null,
    var contact: ContactInfo = ContactInfo()
)

data class ContactInfo (
    var slack: String? = null,
    var msteams: MSTeamsContactInfo = MSTeamsContactInfo("", ""),
    @field:Email var email: String? = null
)

data class MSTeamsContactInfo (
    var name: String? = null,

    @field:Pattern(regexp = "https?://teams.microsoft.com/l/channel/.*", message = "Must be a valid link to an MS Teams channel")
    var link: String? = null
)


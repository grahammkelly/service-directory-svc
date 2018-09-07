package com.travelport.service.directory.model

import javax.validation.constraints.Email
import javax.validation.constraints.Pattern

data class ContactInfo (
    var slack: String = "",
    var msteams: MSTeamsContactInfo = MSTeamsContactInfo("", ""),
    @field:Email var email: String = ""
)

data class MSTeamsContactInfo (
    var name: String,

    @field:Pattern(regexp = "https?://teams.microsoft.com/l/channel/.*", message = "Must be a valid link to an MS Teams channel")
    var link: String
)

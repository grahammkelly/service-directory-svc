package com.travelport.service.glossary.api

import com.travelport.service.glossary.model.enums.ProjectType
import javax.validation.constraints.NotEmpty

/**
 * The field validations are applied only on receipt from an HTTP
 * request. You can still generate the class with empty values, hence
 * the default values
 */
open class ProjectDesc @JvmOverloads constructor (
  @field:NotEmpty var platform: String = "",
  @field:NotEmpty var type: String = "",
  @field:NotEmpty var owner: String = ""
) {
  val projectType =
      ProjectType.matching(platform, type)

  val jenkinsLinkFragment: String
      get() = "job/${platform}/job/${type}"

  val jenkinsBuildBadgeJob: String
    get() = "${platform}/${type}"

}

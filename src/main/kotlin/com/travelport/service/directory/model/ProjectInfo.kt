package com.travelport.service.directory.model

import com.travelport.service.directory.model.ProjectType.SERVICE
import com.travelport.service.directory.model.ProjectType.valueOf
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

data class ProjectInfo (
    @field:NotEmpty var type: String = "",
    var projectType: ProjectType = SERVICE
    var displayName: String? = null,
    var desc: String? = null,
    var owner: Owner? = null,
    var tags: Set<String> = HashSet<String>(),
    var related: RelatedLinks? = RelatedLinks()
) {
  fun cleanUp(repoName: String): ProjectInfo {
    displayName = displayName ?: repoName
    projectType = valueOf(type.toUpperCase())

    return this
  }
}

data class RelatedLinks (
    var dependsOn: Set<String> = HashSet<String>(),
    var dependencyOf: Set<String> = HashSet<String>(),
    var links: Set<LinkInfo> = HashSet<LinkInfo>()
)

data class LinkInfo (
    @field:NotEmpty var name: String = "",
    @field:Pattern(regexp = "https?://.*", message = "Link must be a valid URL") var link: String = ""
): Comparable<LinkInfo> {
  //LinkInfo only depends on 'name' to define whether it's equivalent to another link. Ensure hashCode and equals know this, and ordering reflects it too!
  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (!(other is LinkInfo)) return false
    return name == other.name
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }

  override fun compareTo(other: LinkInfo): Int {
    return name.compareTo(other.name)
  }
}

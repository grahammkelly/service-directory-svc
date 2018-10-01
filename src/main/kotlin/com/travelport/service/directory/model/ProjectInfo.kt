package com.travelport.service.directory.model

import com.travelport.service.directory.model.enums.ProjectType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.Objects
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

//
// Can't use 'data class' here as kotlin makes clases final by default (and explicitly final for data classes).
// Spring cannot apply it's enhancements over final classes. The all-open gradle plugin should fix this (and the
// kotlin spring plugin), however, in this case cannot be applied. I don't have the time to investigate, so not
// using data classes. This means I need to implement equals and hashCode myself, though.
//
@Document(collection = "projects")
open class ProjectInfo @JvmOverloads constructor(
    @field:Id var id: Long = -1L,
    var name: String = "",
    @field:NotEmpty var type: String = "",
    var displayName: String? = null,
    var desc: String? = null,
    var owner: ProjectTeam? = null,
    var tags: Set<String> = emptySet(),
    var related: RelatedLinks = RelatedLinks(),
    var repository: String? = null,
    var product: String = "core",
    var serviceCode: String = ""
) {

  //
  // If the model here changes and the older version (from Mongo) does not map correctly to the new
  // data structure, use a mapping constructor like the following:
  //
  //  ```
  //  @PersistenceConstructor constructor(val somethingFromOldModel: SomeRandomType) :
  //      this(/*default constructor, leaving properties blank if needed*/) {
  //    //Now do your mapping here!
  //  }
  //  ```
  //

  fun cleanUp(repoName: String, gitBaseAddr: String): ProjectInfo {
    displayName = displayName ?: repoName
    name = repoName
    type = try {
      ProjectType.fromString(type).toString()
    } catch (e: IllegalArgumentException) {
      ""
    }
    repository = gitBaseAddr + repoName

    return this
  }

  fun importantInfo() =
      "id: ${id}, name: '${name}', type: '${type}', project: '${product}',${serviceCodeStr} repository: '${repository}'"

  private val serviceCodeStr: String
      get() =
        if (isService) " serviceCode: '${serviceCode}'," else ""

  val isService: Boolean
    get() = (ProjectType.SERVICE == ProjectType.fromString(type))

  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (other === this) return true
    if (other !is ProjectInfo) return false

    return id == other.id && name == other.name && type == other.type &&
        displayName == other.displayName && desc == other.desc &&
        owner == other.owner &&
        tags == other.tags && related == other.related &&
        repository == other.repository &&
        product == other.product && serviceCode == other.serviceCode
  }

  override fun hashCode() =
      Objects.hash(id, name, type, displayName, desc, owner, tags, related, repository, product, serviceCode)

  //Duplicate 'copy' for interop with java. Used for test only!
  fun javaCopy() =
      ProjectInfo(id, name, type, displayName, desc, owner?.copy(), tags, related.copy(), repository, product,
          serviceCode)

  fun containsTag(tag: String) = (!tags.isEmpty()) && tags.contains(tag)

  fun addTag(newTag: String) {
    tags = tags.toMutableSet().apply { add(newTag) }
  }
}

data class RelatedLinks @JvmOverloads constructor(
    var dependsOn: Set<String>? = emptySet(),
    var dependencyOf: Set<LinkInfo> = emptySet(),
    var links: Set<LinkInfo> = emptySet(),
    var dependsUpon: Set<LinkInfo>? = emptySet()
)

data class LinkInfo @JvmOverloads constructor(
    @field:NotEmpty var name: String = "",
    @field:Pattern(regexp = "https?://.*", message = "Link must be a valid URL") var link: String = ""
): Comparable<LinkInfo> {
  //LinkInfo only depends on 'name' to define whether it's equivalent to another link. Ensure hashCode and equals know this, and ordering reflects it too!
  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (other === this) return true
    if (other !is LinkInfo) return false
    return name == other.name
  }

  override fun hashCode() = name.hashCode()

  override fun compareTo(other: LinkInfo) = name.compareTo(other.name)
}


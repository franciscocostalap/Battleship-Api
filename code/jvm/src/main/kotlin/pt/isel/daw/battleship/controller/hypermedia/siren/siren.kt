package pt.isel.daw.battleship.controller.hypermedia.siren

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

private const val APPLICATION_TYPE = "application"
private const val SIREN_SUBTYPE = "vnd.siren+json"

/**
 * For details regarding the Siren media type, see <a href="https://github.com/kevinswiber/siren">Siren</a>
 */
const val SirenContentType = "$APPLICATION_TYPE/$SIREN_SUBTYPE"

/**
 * Gets a Siren self link for the given URI
 *
 * @param uri   the string with the self URI
 * @return the resulting siren link
 */
@JsonInclude(NON_NULL)fun selfLink(uri: String) = SirenLink(rel = listOf("self"), href = uri)

/**
 * Class whose instances represent links as they are represented in Siren.
 */
@JsonInclude(NON_NULL)
data class SirenLink(
    val rel: List<String>,
    val href: String,
    val title: String? = null,
    val type: String? = null
)

/**
 * Class whose instances represent actions that are included in a siren entity.
 */
@JsonInclude(NON_NULL)
data class SirenAction(
    val name: String,
    val href: String,
    val title: String? = null,
    @JsonProperty("class")
    val clazz: List<String>? = null,
    val method: String? = null,
    val type: String? = null,
    val fields: List<Field>? = null
) {

    /**
     * Represents action's fields
     */
    @JsonInclude(NON_NULL)
    data class Field(
        val name: String,
        val type: String? = null,
        val value: String? = null,
        val title: String? = null
    )

}

@JsonInclude(NON_NULL)
data class SirenEntity<T>(
    @JsonProperty("class") val clazz: List<String>? = null,
    val properties: T? = null,
    val entities: List<SubEntity>? = null,
    val links: List<SirenLink>? = null,
    val actions: List<SirenAction>? = null,
    val title: String? = null
)

/**
 * Base class for admissible sub entities, namely, [EmbeddedLink] and [EmbeddedEntity].
 * Notice that this is a closed class hierarchy.
 */
@JsonSubTypes(
    JsonSubTypes.Type(value = EmbeddedLink::class, name = "link"),
    JsonSubTypes.Type(value = EmbeddedEntity::class, name = "entity")
)
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
sealed class SubEntity

@JsonInclude(NON_NULL)
data class EmbeddedLink(
    @JsonProperty("class")
    val clazz: List<String>? = null,
    val rel: List<String>,
    val href: String,
    val type: String? = null,
    val title: String? = null
) : SubEntity()

@JsonInclude(NON_NULL)
data class EmbeddedEntity<T>(
    val rel: List<String>,
    @JsonProperty("class") val clazz: List<String>? = null,
    val properties: T? = null,
    val entities: List<SubEntity>? = null,
    val links: List<SirenLink>? = null,
    val actions: List<SirenAction>? = null,
    val title: String? = null
) : SubEntity()


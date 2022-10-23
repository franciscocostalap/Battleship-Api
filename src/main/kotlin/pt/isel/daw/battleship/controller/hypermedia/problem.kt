package pt.isel.daw.battleship.controller.hypermedia

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.net.URI


private const val APPLICATION_TYPE = "application"
private const val PROBLEM_SUBTYPE = "problem+json"

/**
 * For more information about the problem details specification, see https://datatracker.ietf.org/doc/html/rfc7807#section-3
 */
const val ProblemMediaType = "$APPLICATION_TYPE/$PROBLEM_SUBTYPE"


fun ResponseEntity.BodyBuilder.setProblemHeader() = contentType(MediaType.valueOf(ProblemMediaType))

@JsonInclude(NON_NULL)
data class Problem(
    val type : URI? = null,
    val title : String? = null,
    val detail : String? = null,
    val instance : String? = null,
)
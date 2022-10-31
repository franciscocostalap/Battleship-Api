package pt.isel.daw.battleship.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.StatusAssertions
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pt.isel.daw.battleship.controller.Uris
import pt.isel.daw.battleship.controller.hypermedia.ProblemContentType
import pt.isel.daw.battleship.controller.hypermedia.siren.SirenEntity
import pt.isel.daw.battleship.repository.*
import pt.isel.daw.battleship.services.entities.AuthInformation

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiTests {

    @LocalServerPort
    var port: Int = 0

    @TestConfiguration
    class Config{
        @Bean
        @Primary
        fun getTransactionFactory() = JdbiTransactionFactoryTestDB()
    }


    @BeforeEach
    fun executeClean() {
        executeWithHandle { handle ->
            clear()
        }
    }

    private val client by lazy {
        WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port/api")
            .build()
    }
    companion object{
        private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    }
    @Test
    fun `a request for a path that does not exist returns 404`() {
        client.get().uri("/does-not-exist")
            .exchange()
            .expectStatus().isNotFound
            .expectHeader()
            .value("content-type") {
                assertTrue(it.equals("application/problem+json"))
            }
    }

    @Test
    fun `Method not supported on a path with a different method other then the one supported returns 405`() {
        client.post().uri(Uris.Home.SYSTEM_INFO)
            .exchange()
            .expectStatus().isEqualTo(405)
            .expectHeader()
            .value("content-type") {
                assertTrue(it.equals(ProblemContentType))
            }
    }

    @Test
    fun `get server stats ok`(){
        client.get().uri(Uris.Home.SYSTEM_INFO)
            .exchange()
            .expectStatus().isOk
            .expectHeader()
            .assertContentTypeSiren()
            .expectBody()
            .jsonPath("\$.properties.version").isEqualTo("0.0.2")
    }


    @Test
    fun `create a user returns 201`(){
        client.post().uri(Uris.User.REGISTER)
            .bodyValue("""{"username":"test","password":"testad1"}""")
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isCreated
            .expectHeader()
            .assertContentTypeSiren()
            .expectBody()
            .jsonPath("\$.properties.uid").isNumber
    }

    @Test
    fun `invalid params on body returns 400`(){
        client.post().uri(Uris.User.REGISTER)
            .bodyValue("""{"username":"abc","password":""}""")
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader()
            .assertContentTypeProblem()
    }

    //todo move to utils
    val StatusAssertions.isConflict
        get() = isEqualTo(HttpStatus.CONFLICT)


    @Test
    fun `creating a user with a repeated username returns 409`(){
        val auth = createUser("test","testad1")

        client.post().uri(Uris.User.REGISTER)
            .bodyValue("""{"username":"test","password":"testad1"}""")
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isConflict
            .expectHeader()
            .assertContentTypeProblem()
    }


    private fun createUser(username: String, password: String): AuthInformation {
        val body = client.post().uri(Uris.User.REGISTER)
            .bodyValue("""{"username":"$username","password":"$password"}""")
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isCreated
            .expectHeader()
            .assertContentTypeSiren()
            .expectBody<SirenEntity<AuthInformation>>()
            .returnResult()
            .responseBody!!
        val id = body.properties?.uid
        val token = body.properties?.token

        return AuthInformation(id!!, token!!)
    }
}
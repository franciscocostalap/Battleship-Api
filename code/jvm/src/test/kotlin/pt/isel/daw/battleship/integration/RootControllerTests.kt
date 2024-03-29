package pt.isel.daw.battleship.integration

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pt.isel.daw.battleship.controller.Uris
import pt.isel.daw.battleship.controller.hypermedia.ProblemContentType
import pt.isel.daw.battleship.controller.hypermedia.siren.SirenEntity
import pt.isel.daw.battleship.controller.hypermedia.siren.siren_navigation.builders.NoEntitySiren
import pt.isel.daw.battleship.repository.jdbiTransactionFactoryTestDB
import pt.isel.daw.battleship.repository.clear
import pt.isel.daw.battleship.repository.executeWithHandle
import pt.isel.daw.battleship.services.entities.Statistics

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RootControllerTests {

    @LocalServerPort
    var port: Int = 0

    @TestConfiguration
    class Config{
        @Bean
        @Primary
        fun getTransactionFactory() = jdbiTransactionFactoryTestDB()
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

    @Test
    fun `a request for a path that does not exist returns 404`() {
        client.get().uri("/does-not-exist")
            .exchange()
            .expectStatus().isNotFound
            .expectHeader()
            .value("content-type") {
                assertTrue(it.equals(ProblemContentType))
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
    fun `get server stats sucessfully`(){
        client.get().uri(Uris.Home.SYSTEM_INFO)
            .exchange()
            .expectStatus().isOk
            .expectHeader()
            .assertContentTypeSiren()
            .expectBody()
            .jsonPath("\$.properties.version").isEqualTo("0.0.2")
    }

    @Test
    fun `get user ranking without any games played`(){
        client.get().uri(Uris.Home.STATISTICS)
            .exchange()
            .expectStatus().isOk
            .expectHeader()
            .assertContentTypeSiren()
            .expectBody<SirenEntity<Statistics>>()
            .returnResult()
            .responseBody
    }


    @Test
    fun `Get home representation`(){
        client.get().uri(Uris.Home.ROOT)
            .exchange()
            .expectStatus().isOk
            .expectHeader()
            .assertContentTypeSiren()
            .expectBody<SirenEntity<NoEntitySiren>>()
            .returnResult()
            .responseBody
    }


}
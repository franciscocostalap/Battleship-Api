package pt.isel.daw.battleship.controller.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.battleship.controller.Uris
import pt.isel.daw.battleship.controller.hypermedia.SirenEntity
import pt.isel.daw.battleship.services.GeneralService
import pt.isel.daw.battleship.services.entities.GameStatistics
import pt.isel.daw.battleship.services.entities.SystemInfo

@RestController
@RequestMapping(Uris.HOME)
class RootController(
    val generalService: GeneralService
) {

    @GetMapping(Uris.SYSTEM_INFO)
    fun getSystemInfo(): SirenEntity<SystemInfo>{
        val sysInfo = generalService.getSystemInfo()

        return SirenEntity(
            properties = sysInfo,
            title = "System Info",
            entities = listOf(),
            actions = listOf(),
            links = listOf()
        )
    }

    @GetMapping(Uris.STATISTICS)
    fun getStatistics(): SirenEntity<GameStatistics>{
        val statistics = generalService.getStatistics()

        return SirenEntity(
            properties = statistics,
            title = "Statistics",
            entities = listOf(),
            actions = listOf(),
            links = listOf()
        )
    }
}

package pt.isel.daw.battleship.controller.controllers

import org.springframework.web.bind.annotation.*
import pt.isel.daw.battleship.controller.Uris
import pt.isel.daw.battleship.controller.dto.BoardDTO
import pt.isel.daw.battleship.controller.dto.input.LayoutInfoInputModel
import pt.isel.daw.battleship.controller.dto.input.ShotsInfoInputModel
import pt.isel.daw.battleship.controller.hypermedia.siren.*
import pt.isel.daw.battleship.controller.hypermedia.siren.siren_navigation.builders.NoEntitySiren
import pt.isel.daw.battleship.controller.pipeline.authentication.Authentication
import pt.isel.daw.battleship.services.GameService
import pt.isel.daw.battleship.services.entities.GameRulesDTO
import pt.isel.daw.battleship.services.entities.GameStateInfo
import pt.isel.daw.battleship.utils.UserID

@RestController
class GameController(
    val gameService: GameService
) {

    @Authentication
    @GetMapping(Uris.Game.FLEET)
    fun getFleetState(
        @PathVariable("gameId") gameID: Int,
        userID: UserID,
        @PathVariable("whichFleet") fleet: String
    ): SirenEntity<BoardDTO> {
        val board = gameService.getFleetState(userID, gameID, whichFleet = fleet)
        return board.appToSiren(AppSirenNavigation.FLEET_NODE_KEY, mapOf("gameId" to gameID.toString()))
    }

    @Authentication
    @PostMapping(Uris.Game.SHOTS_DEFINITION)
    fun defineShots(
        @RequestParam(required = false) embedded : Boolean,
        @PathVariable("gameId") gameID: Int,
        userID: UserID,
        @RequestBody input: ShotsInfoInputModel
    ): SirenEntity<NoEntitySiren> {
        gameService.makeShots(userID, gameID, input.shots)

        val siren = noEntitySiren(
            AppSirenNavigation.graph,
            AppSirenNavigation.SHOTS_DEFINITION_NODE_KEY
        )

        return if(embedded){
            val embeddableBoard = gameService.getFleetState(userID, gameID, whichFleet = "opponent")

            siren.appAppendEmbedded(
                AppSirenNavigation.FLEET_NODE_KEY,
                embeddableBoard,
                AppSirenNavigation.SHOTS_DEFINITION_NODE_KEY
            )
        }else{
            siren
        }
    }

    @Authentication
    @PostMapping(Uris.Game.LAYOUT_DEFINITION)
    fun defineLayout(
        @PathVariable("gameId") gameID: Int,
        userID: UserID,
        @RequestBody input: LayoutInfoInputModel
    ): SirenEntity<NoEntitySiren> {
        gameService.defineFleetLayout(userID, gameID, input.shipsInfo)

        return noEntitySiren(
            AppSirenNavigation.graph,
            AppSirenNavigation.DEFINE_LAYOUT_NODE_ID
        )
    }

    @Authentication
    @GetMapping(Uris.Game.STATE)
    fun getGameState(@PathVariable("gameId") gameID: Int, userID: UserID): SirenEntity<GameStateInfo> {
        val state = gameService.getGameState(gameID, userID)
        return state.appToSiren(AppSirenNavigation.GAME_STATE_NODE_KEY)
    }

    @Authentication
    @GetMapping(Uris.Game.RULES)
    fun getGameRules(@PathVariable("gameId") gameID: Int, userID: UserID): SirenEntity<GameRulesDTO> {
        val rules = gameService.getGameRules(gameID, userID)
        return rules.appToSiren(AppSirenNavigation.GAME_RULES_NODE_KEY)
    }

}
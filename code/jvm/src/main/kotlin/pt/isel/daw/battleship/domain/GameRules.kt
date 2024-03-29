package pt.isel.daw.battleship.domain

import pt.isel.daw.battleship.services.secondsToMillis
import pt.isel.daw.battleship.utils.ShipCount
import pt.isel.daw.battleship.utils.ShipSize
import pt.isel.daw.battleship.utils.TimeoutTime

data class GameRules(
    val shotsPerTurn: Int,
    val boardSide: Int,
    val playTimeout: TimeoutTime,
    val layoutDefinitionTimeout: TimeoutTime,
    val shipRules: ShipRules
) {

    data class ShipRules(
        val name: String, val fleetComposition: Map<ShipSize, ShipCount>
    )

    companion object {

        val DEFAULT = GameRules(
            1,
            10,
            secondsToMillis(seconds=60),
            secondsToMillis(seconds=60),
            ShipRules(
                "Classic",
                mapOf<ShipSize, ShipCount>(
                    5 to 1,
                    4 to 1,
                    3 to 1,
                    2 to 1,
                    1 to 1
                )
            )
        )
    }
}




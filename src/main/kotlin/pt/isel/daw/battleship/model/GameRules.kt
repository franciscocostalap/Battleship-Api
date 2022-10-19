package pt.isel.daw.battleship.model

import pt.isel.daw.battleship.utils.ShipCount
import pt.isel.daw.battleship.utils.ShipSize

data class GameRules(
    val shotsPerTurn: Int,
    val boardSide: Int,
    val playTimeout: Int,
    val layoutDefinitionTimeout: Int,
    val shipRules: ShipRules
) {

    data class ShipRules(
        val name: String, val fleetComposition: Map<ShipSize, ShipCount>
    )

    companion object {

        val DEFAULT = GameRules(
            1,
            10,
            60,
            60,
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




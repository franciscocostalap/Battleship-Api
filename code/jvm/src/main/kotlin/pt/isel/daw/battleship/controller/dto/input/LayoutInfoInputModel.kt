package pt.isel.daw.battleship.controller.dto.input

import pt.isel.daw.battleship.domain.board.ShipInfo

data class LayoutInfoInputModel (
    val shipsInfo: List<ShipInfo>
)

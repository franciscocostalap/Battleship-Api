package pt.isel.daw.battleship.repository.dto

import pt.isel.daw.battleship.utils.ID
import pt.isel.daw.battleship.utils.UserID

data class LobbyDTO(
    val id: ID,
    val player1: UserID,
    val player2: UserID?,
    val gameID: ID?
)

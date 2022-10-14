package pt.isel.daw.battleship.services.dto

import pt.isel.daw.battleship.data.model.*
import pt.isel.daw.battleship.services.entities.GameMapper

data class GameDTO(
    val id: Id,
    val state: String,
    val winner: Id?,
    val player1: Id,
    val player2: Id,
){
    constructor(gameEntity: GameMapper): this(
        gameEntity.Id,
        gameEntity.state.toString(),
        gameEntity.winnerID,
        gameEntity.player1ID,
        gameEntity.player2ID,
    )
}

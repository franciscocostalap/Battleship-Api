package pt.isel.daw.battleship.data.model

import pt.isel.daw.battleship.data.Id
import pt.isel.daw.battleship.data.Square

data class Game(
    val Id: Id,
    val state: State,
    val rules: GameRules = GameRules.DEFAULT,
    val boards: List<Board> = List(2){ Board.empty(rules.boardSide) },
    val turnIdx: Int
){

    sealed class Ship(val size: Int)


    enum class State {
        WAITING_PLAYER,
        PLACING_SHIPS,
        PLAYING,
        FINISHED
    }

}

/**
 * Returns a new game after a shot is made on the specified [square]
 *
 * @throws IllegalArgumentException if the square is invalid according to the [Game.rules]
 */
fun Game.makeShot(square: Square): Game = this.copy(
        boards= boards.mapIndexed { idx, board ->
            if(idx == turnIdx) return@mapIndexed board

            board.shotTo(square)
        },
        turnIdx = this.nextTurn()
    )

/**
 * Returns the next turn index
 */
fun Game.nextTurn(): Int = if(turnIdx > 0) 0 else 1



data class GameRules(
    val shotsPerRound: Int,
    val boardSide: Int
) {
    companion object {
        val DEFAULT = GameRules(1, 10)
    }
}


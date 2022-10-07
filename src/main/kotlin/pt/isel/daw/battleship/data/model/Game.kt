package pt.isel.daw.battleship.data.model

import pt.isel.daw.battleship.data.Id
import pt.isel.daw.battleship.data.Square

data class Player (val id : Id, val name : String )

data class Game(
        val Id: Id,
        val state: State,
        val rules: GameRules = GameRules.DEFAULT,
        val boards: List<Board> = List(2){ Board.empty(rules.boardSide) },
        val players : List<Player> = listOf( Player(1,"p1") , Player(2,"p2") ),
        val turnIdx: Int
){
    val turnBoard = boards[turnIdx]
    val oppositeTurnIdx = 1 - turnIdx
    val oppositeTurnBoard = boards[oppositeTurnIdx]
    val turnPlayer  = players[turnIdx]
    val oppositeTurnPlayer = players[oppositeTurnIdx]


    sealed class Ship(val size: Int)
    inner class Carrier() : Ship(0)//rules.shipRules.carrierSize)
    inner class Battleship(size: Int) : Ship(size)
    inner class Cruiser(size: Int) : Ship(size)
    inner class Submarine(size: Int) : Ship(size)
    inner class Destroyer(size: Int) : Ship(size)


    enum class State {
        WAITING_PLAYER,
        PLACING_SHIPS,
        PLAYING,
        FINISHED
    }

}

/**
 * Returns a new game after a shot is made on the specified [Square]
 *
 * @param squares
 * @throws IllegalArgumentException if the square is invalid according to the [Game.rules]
 */
fun Game.makeShot(squares: List<Square>): Game {
    if(squares.size != rules.shotsPerTurn) throw IllegalArgumentException("Invalid number of shots")

    val newBoard = turnBoard.makeShots(squares)
    return this.copyBoard(oppositeTurnIdx, newBoard)
}

/**
 * Returns a new game after placing the ships on the board
 *
 * @throws IllegalArgumentException if the ship is invalid according to the [Game.rules]
 */
fun Game.placeShips(shipList: List<ShipInfo>) : Game {

    rules.verifyShips(shipList)

    val newBoard = this.turnBoard.placeShips(shipList)

    return this.copyBoard(turnIdx,newBoard)
}

fun Game.copyBoard(turnIdx: Int,newBoard: Board) = this.copy(
    boards = boards.mapIndexed { idx, b ->
        if (idx != turnIdx) return@mapIndexed b

        newBoard
    }
)

/**
 * Returns the next turn index
 */
fun Game.nextTurn(): Int = if(turnIdx > 0) 0 else 1


private fun Int.verifyShipSize(size: Int?) {
    size ?: throw IllegalArgumentException("Ship is not accepted with the current game rules")
    if(this != size) throw IllegalArgumentException("Invalid ship size")
}

data class ShipRules(
val carrierSize: Int?,
val battleshipSize: Int?,
val cruiserSize: Int?,
val submarineSize: Int?,
val destroyerSize: Int?
)
data class GameRules(
    val shotsPerTurn: Int,
    val boardSide: Int,
    val maxTimeToPlay : Int,
    val maxTimeToDefineLayout : Int,
    val shipRules : ShipRules
    //fleet composition

) {

    fun verifyShips(shipList: List<ShipInfo>) {
        shipList.forEach { shipInfo ->
            when (val ship = shipInfo.ship) {
                is Game.Carrier -> ship.size.verifyShipSize(shipRules.carrierSize)
                is Game.Battleship -> ship.size.verifyShipSize(shipRules.battleshipSize)
                is Game.Cruiser -> ship.size.verifyShipSize(shipRules.cruiserSize)
                is Game.Submarine -> ship.size.verifyShipSize(shipRules.submarineSize)
                is Game.Destroyer -> ship.size.verifyShipSize(shipRules.destroyerSize)

            }
        }
    }


    companion object {
        val DEFAULT = GameRules(
                1, 10,60,
                60, ShipRules(5,4,
                3,3,2)
        )
    }
}



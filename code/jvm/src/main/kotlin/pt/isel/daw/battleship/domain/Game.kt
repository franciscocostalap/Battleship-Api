
package pt.isel.daw.battleship.domain

import pt.isel.daw.battleship.domain.board.*
import pt.isel.daw.battleship.utils.ID
import pt.isel.daw.battleship.utils.TimeoutTime
import pt.isel.daw.battleship.utils.UserID

/**
 * Represents a state of a battleship game.
 */
data class Game(
    val id: ID?,
    val state: State,
    val rules: GameRules,
    val playerBoards: Map<UserID, Board>,
    val turnID: UserID,
    val lastUpdated: TimeoutTime = System.currentTimeMillis()
) {

    companion object;

    init {
        val playerBoards = playerBoards.values


        requireGameRule(playerBoards.all { it.side == rules.boardSide }) {
            "Board's side length is different from the rules"
        }

        require(this.playerBoards.size == 2){ "Game must have exactly 2 players" }

        // Check fleet composition
        if (state == State.PLAYING)
            check(playerBoards.all { it.fleetComposition == rules.shipRules.fleetComposition }) {
                "Fleet composition is different from the rules"
            }
    }

    val oppositeTurnID by afterGameBegins { playerBoards.keys.first { it != turnID } }

    val oppositeTurnBoard: Board by afterGameBegins {
        playerBoards[oppositeTurnID] ?: error("No board for the opposite turn ID")
    }

    /**
     * Returns a lazy property delegate that is only available after the game has begun.
     * @throws IllegalStateException if the game has not yet begun.
     */
    private fun <T> afterGameBegins(initializer: () -> T): Lazy<T> {
        return lazy {
            check(playerBoards.size == 2 && (state == State.PLAYING || state == State.FINISHED)) {
                "Can't access this property before the game begins."
            }
            initializer()
        }
    }

    /**
     * Represents the possible States of a game.
     */
    enum class State {
        PLACING_SHIPS,
        PLAYING,
        FINISHED,
        CANCELLED
    }
    val player1ID: UserID by lazy { playerBoards.keys.first() }
    val player2ID: UserID by lazy { playerBoards.keys.last() }
}


/**
 * Returns a new game after a shot is made on the specified [Square]
 *
 * @param squares the squares to shoot on
 * @throws IllegalArgumentException if a different number of shots is made than the rules allow
 * or if the game is not in the [Game.State.PLAYING] state
 */
fun Game.makePlay(squares: List<Square>): Game {
    requireGameState(Game.State.PLAYING)

    if (ranOutOfTime()) {
        return this.copy(state = Game.State.CANCELLED)
    }

    requireGameRule(squares.size == rules.shotsPerTurn){
        "A play requires exactly ${rules.shotsPerTurn} shots."
    }

    val newBoard = oppositeTurnBoard.makeShots(squares)
    val gameWithNewBoards = replaceBoard(oppositeTurnID, newBoard)

    return gameWithNewBoards
        .copy(
            turnID = oppositeTurnID,
            state =
            if (gameWithNewBoards.playerBoards.values.any { it.isInEndGameState() })
                Game.State.FINISHED
            else
                state,
            lastUpdated = System.currentTimeMillis()
        )
}

/**
 * Returns a new fresh game
 */
fun Game.Companion.new(players: Pair<UserID, UserID>, rules: GameRules) = Game(
    id = null,
    state = Game.State.PLACING_SHIPS,
    rules = rules,
    playerBoards = mapOf(
        players.first to Board.empty(rules.boardSide),
        players.second to Board.empty(rules.boardSide)
    ),
    turnID = players.first,
    lastUpdated = System.currentTimeMillis()
)

/**
 * Checks whether the game has ran out of time for the specified timeout.
 */
private fun Game.ranOutOfTime(): Boolean{
    val remainingTime = remainingTime()
    return remainingTime == null || remainingTime <= 0
}

/**
 * Returns the remaining time for the current game phase.
 *
 * @return the remaining time in milliseconds
 */
fun Game.remainingTime(): TimeoutTime? {
    val timeout = when (state) {
        Game.State.PLAYING -> rules.playTimeout
        Game.State.PLACING_SHIPS -> rules.layoutDefinitionTimeout
        else -> return null
    }

    return timeout - (System.currentTimeMillis() - lastUpdated)
}

/**
 * Returns a new game after placing the ships on the board
 * @param shipList the list of ships to place
 * @param playerID the player that is placing the ships
 * @throws GameRuleViolationException if the ship is invalid according to the [Game.rules]
 */
fun Game.placeShips(shipList: List<ShipInfo>, playerID: UserID): Game {
    requireGameState(Game.State.PLACING_SHIPS)

    requireGameRule(this.playerBoards[playerID]?.hasShips() != true) {
        "You already placed your ships."
    }

    if (ranOutOfTime()) {
        return this.copy(state = Game.State.CANCELLED)
    }


    val emptyBoard = Board.empty(this.rules.boardSide)
    val newBoard = emptyBoard.placeShips(shipList)

    requireGameRule(newBoard.fleetComposition == rules.shipRules.fleetComposition){
        "Require the following fleet composition: " +
        rules.shipRules.fleetComposition.entries.joinToString(", "){
            "${it.value} boats of ${it.key} squares."
        }
    }

    val newGameState = this.replaceBoard(playerID, newBoard)
    val hasBothBoardsNotEmpty = newGameState.playerBoards.values.all { it != emptyBoard }

    return if (hasBothBoardsNotEmpty)
        newGameState.copy(state = Game.State.PLAYING, lastUpdated = System.currentTimeMillis())
    else
        newGameState

}

/**
 * Returns a new Game after the board from [id] is replaced by [newBoard]
 * @param id the player whose board is to be replaced
 * @param newBoard the new board
 * @return [Game] a new Game with the new board
 */
private fun Game.replaceBoard(id: UserID, newBoard: Board) = copy(
    playerBoards = this.playerBoards.mapValues { entry ->
        if (entry.key == id)
            newBoard
        else
            entry.value
    }
)






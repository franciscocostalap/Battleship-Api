package pt.isel.daw.battleship.repository

import pt.isel.daw.battleship.data.model.Id
import pt.isel.daw.battleship.data.model.Game

interface GameRepository {

    fun getNumOfGames(): Int

    /**
     * Gets the [Game.State] of a game and the Winner id if the state is [Game.State.FINISHED]
     * @param gameId the id of the game
     */
    fun getGameState(gameId: Id): Pair<Game.State, Id?>


    fun getGames(): List<Game>

    /**
     * Gets the [Game] with the given id
     */
    fun getGame(gameId: Id): Game?


    fun updateGame(gameId: Id, game: Game)

    /**
     * Verifies if the game with the given id exists
     */
    fun hasGame(gameId: Id): Boolean

    /**
     * Verifies wheter the userId received is on its turn to play
     * @return TRUE if the user is on its turn, FALSE otherwise
     */
    fun verifyTurn(userId: Id, gameId: Id) : Boolean
}
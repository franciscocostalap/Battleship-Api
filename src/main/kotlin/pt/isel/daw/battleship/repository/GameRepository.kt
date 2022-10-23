package pt.isel.daw.battleship.repository

import pt.isel.daw.battleship.model.Game
import pt.isel.daw.battleship.model.Id
import pt.isel.daw.battleship.repository.dto.*
import pt.isel.daw.battleship.services.entities.GameStatistics

interface GameRepository {
    /**
     * Gets the game with the given id
     * @param gameID the id of the game
     * @return [Game] the game
     */
    fun get(gameID: Id): Game?

    /**
     * Persists the given game in the database
     * @param game the game to be persisted
     * @return [Id] of the game persisted
     */
    fun persist(game: GameDTO): Id?

    /**
     * Gets information about the system
     */
    fun getSystemInfo(): SystemInfo

    /**
     * Gets the game statistics
     */
    fun getStatistics(): GameStatistics
}
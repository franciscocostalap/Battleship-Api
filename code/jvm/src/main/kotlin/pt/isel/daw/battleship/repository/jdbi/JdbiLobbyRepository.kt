package pt.isel.daw.battleship.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.isel.daw.battleship.repository.LobbyRepository
import pt.isel.daw.battleship.repository.dto.LobbyDTO
import pt.isel.daw.battleship.utils.ID
import pt.isel.daw.battleship.utils.UserID

class JdbiLobbyRepository(
    private val handle: Handle
): LobbyRepository {


    /**
     * Adds a player to the waiting lobby.
     * @param userID The player's ID.
     */
    override fun createLobby(userID: UserID): ID {
        return handle.createUpdate("INSERT INTO waitinglobby(player1) VALUES (:userID)")
            .bind("userID", userID)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Int::class.java)
            .first()
    }

    /**
     *
     */
    override fun completeLobby(lobbyID: ID, player2: UserID, gameID: ID): Boolean {
        return handle.createUpdate("UPDATE waitinglobby SET player2 = :player2, gameID = :gameid WHERE id = :lobbyID")
            .bind("player2", player2)
            .bind("gameid", gameID)
            .bind("lobbyID", lobbyID)
            .execute() == 1
    }

    /**
     * Removes a player from the waiting lobby.
     * @param userID The player's ID.
     * @return [Boolean]
     */
    override fun removePlayerFromLobby(userID: UserID): Boolean {
        return handle.createUpdate("DELETE FROM waitinglobby WHERE id = " +
                "(SELECT min(id) FROM waitinglobby WHERE player1 = :userID)")
            .bind("userID", userID)
            .execute() == 1
    }

    /**
     * Gets the first lobby in the waiting list.
     */
    override fun findWaitingLobby(): LobbyDTO? {
        return handle.createQuery("SELECT * FROM waitinglobby WHERE player2 IS NULL")
            .mapTo(LobbyDTO::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun get(lobbyId: ID): LobbyDTO? {
        return handle.createQuery("SELECT * FROM waitinglobby WHERE id = :lobbyId")
            .bind("lobbyId", lobbyId)
            .mapTo(LobbyDTO::class.java)
            .findFirst()
            .orElse(null)
    }


}
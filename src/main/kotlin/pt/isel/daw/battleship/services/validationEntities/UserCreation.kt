package pt.isel.daw.battleship.services.validationEntities

import pt.isel.daw.battleship.services.hashPassword
import pt.isel.daw.battleship.services.requireParameter


class UserCreation(
    username: String?,
    password: String?,
) {

    val username: String
    val password_hash: String

    init {
        val safePassword = requireParameter(password, "password")
        val safeUsername = requireParameter(username, "username")

        require(safePassword.length >= 8) { "Password must be at least 8 characters long" }
        require(safeUsername.length >= 3) { "Username must be at least 3 characters long" }

        this.username = safeUsername
        this.password_hash = hashPassword(safePassword)
    }

}
